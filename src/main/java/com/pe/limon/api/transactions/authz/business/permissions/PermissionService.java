package com.pe.limon.api.transactions.authz.business.permissions;

import com.github.benmanes.caffeine.cache.Cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService implements IPermissionService {

    private final Cache<String, EffectivePerm> cache;
    private final NamedParameterJdbcTemplate jdbc;

    private String key(long eventId, String userId) {
        return eventId + ":" + userId;
    }

    @Override
    public EffectivePerm getEffective(long eventId, String userId) {
        String cacheKey = key(eventId, userId);
        var cached = cache.getIfPresent(cacheKey);
        if (cached != null) return cached;

        final String sql = """
        SELECT
          e.id AS event_id,
          e.name,
          e.start_datetime,
          e.payment_mode,
          e.qr_enabled,
          e.time_zone,
          IF(e.owner_user_id = :userId, 1, 0) AS is_owner,

          se.id AS scope_event_id,
          sg.id AS scope_global_id,
          COALESCE(se.id, sg.id) AS effective_scope_id,

          CASE
            WHEN e.owner_user_id = :userId THEN 1
            WHEN admin_scope.scope_id IS NOT NULL THEN 1
            ELSE 0
          END AS is_admin

        FROM tbl_event e

        LEFT JOIN tbl_collaborator c
          ON c.owner_user_id = e.owner_user_id
         AND c.user_id = :userId

        LEFT JOIN tbl_collaborator_scope se
          ON se.collaborator_id = c.id
         AND se.scope_type = 'EVENT'
         AND se.event_id = e.id

        LEFT JOIN tbl_collaborator_scope sg
          ON sg.collaborator_id = c.id
         AND sg.scope_type = 'GLOBAL'
         AND sg.event_id IS NULL

        LEFT JOIN (
           SELECT DISTINCT csr.scope_id
           FROM tbl_collaborator_scope_role csr
           JOIN tbl_role r ON r.id = csr.role_id
           WHERE r.name = 'ADMIN'
        ) admin_scope
          ON admin_scope.scope_id = COALESCE(se.id, sg.id)

        WHERE e.id = :eventId
        """;

        var params = new MapSqlParameterSource()
                .addValue("eventId", eventId)
                .addValue("userId", userId);

        var rows = jdbc.query(sql, params, (rs, n) -> {
            long eid = rs.getLong("event_id");
            String name = Optional.ofNullable(rs.getString("name")).orElse("");
            String timeZone = Optional.ofNullable(rs.getString("time_zone")).orElse("");
            String paymentMode = Optional.ofNullable(rs.getString("payment_mode")).orElse("");
            boolean qrEnabled = rs.getInt("qr_enabled") == 1;

            boolean isOwner = rs.getInt("is_owner") == 1;
            boolean isAdmin = rs.getInt("is_admin") == 1;

            Long effectiveScopeId = rs.getLong("effective_scope_id");
            if (rs.wasNull()) effectiveScopeId = null;
            var startDatetime = rs.getString("start_datetime");
            return new Row(eid, name, paymentMode, startDatetime, timeZone,qrEnabled, isOwner, isAdmin, effectiveScopeId);
        });

        if (rows.isEmpty()) {
            var empty = EffectivePerm.empty(eventId);
            cache.put(cacheKey, empty);
            return empty;
        }

        Row r = rows.get(0);

        // Si no hay scope efectivo => sin permisos (si esOwner, is_admin ya viene 1 igual)
        if (r.effectiveScopeId == null) {
            var eff = new EffectivePerm(
                    r.eventId, r.eventName, r.startDatetime,r.paymentMode, r.timeZone,r.qrEnabled,
                    r.isOwner,
                    r.isAdmin, // puede ser true si esOwner
                    Collections.emptySet()
            );
            cache.put(cacheKey, eff);
            return eff;
        }

        // Cargar módulos del scope efectivo
        Set<String> modules = loadModulesByScope(r.effectiveScopeId);

        var eff = new EffectivePerm(
                r.eventId, r.eventName, r.paymentMode, r.timeZone,r.startDatetime,r.qrEnabled,
                r.isOwner,
                r.isAdmin,   // ✅ ya viene del SQL (owner o rol ADMIN)
                modules
        );

        cache.put(cacheKey, eff);
        return eff;
    }

    @Override
    public boolean can(long eventId, String userId, String module) {
        var eff = getEffective(eventId, userId);
        return eff.isOwner() || eff.isAdmin() || eff.hasModule(module);
    }

    @Override
    public void invalidate(long eventId, String userId) {
        cache.invalidate(key(eventId, userId));
    }

    @Override
    public Map<Long, EffectivePerm> getAllEffective(String userId) {
        final String sql = """
            SELECT
              e.id AS event_id,
              e.name,
              e.time_zone,
              e.payment_mode,
              e.qr_enabled,
              e.start_datetime,
              IF(e.owner_user_id = :userId, 1, 0) AS is_owner,
              se.id AS scope_event_id,
              sg.id AS scope_global_id,
              COALESCE(se.id, sg.id) AS effective_scope_id,
              CASE
                WHEN e.owner_user_id = :userId THEN 1
                WHEN admin_scope.scope_id IS NOT NULL THEN 1
                ELSE 0
              END AS is_admin
            FROM tbl_event e
            
            LEFT JOIN tbl_collaborator c
                ON c.owner_user_id = e.owner_user_id
                AND c.user_id = :userId
            LEFT JOIN tbl_collaborator_scope se
                ON se.collaborator_id = c.id
                AND se.scope_type = 'EVENT'
                AND se.event_id = e.id
            LEFT JOIN tbl_collaborator_scope sg
                ON sg.collaborator_id = c.id
                AND sg.scope_type = 'GLOBAL'
                AND sg.event_id IS NULL
            LEFT JOIN (
                SELECT DISTINCT csr.scope_id
                FROM tbl_collaborator_scope_role csr
                WHERE csr.role_id = 1
            )   admin_scope
              ON admin_scope.scope_id = COALESCE(se.id, sg.id)
            WHERE
              e.owner_user_id = :userId
              OR se.id IS NOT NULL
              OR sg.id IS NOT NULL;
            """;

        var params = new MapSqlParameterSource().addValue("userId", userId);
        var map = new HashMap<Long, EffectivePerm>(64);

        jdbc.query(sql, params, rs -> {
            long eventId = rs.getLong("event_id");
            String eventName = Optional.ofNullable(rs.getString("name")).orElse("");
            String timeZone = Optional.ofNullable(rs.getString("time_zone")).orElse("");
            String paymentMode = Optional.ofNullable(rs.getString("payment_mode")).orElse("");
            boolean qrEnabled = rs.getInt("qr_enabled") == 1;

            boolean isOwner = rs.getInt("is_owner") == 1;
            boolean isAdmin = rs.getInt("is_admin") == 1;
            String startDatetime = rs.getString("start_datetime");
            if (startDatetime != null){
                startDatetime = startDatetime.substring(0,10);
            }
            // Para saber si este row venía por EVENT o por GLOBAL (para la precedencia)
            Long scopeEventId = rs.getLong("scope_event_id");
            if (rs.wasNull()) scopeEventId = null;

            Long effectiveScopeId = rs.getLong("effective_scope_id");
            if (rs.wasNull()) effectiveScopeId = null;

            EffectivePerm current = map.get(eventId);

            // 1) Owner > todo (siempre gana)
            if (isOwner) {
                var eff = new EffectivePerm(
                        eventId, eventName,startDatetime, paymentMode, timeZone,qrEnabled,
                        true,  // isOwner
                        true,  // isAdmin (owner engloba admin, como definiste)
                        Collections.emptySet()
                );
                map.put(eventId, eff);
                cache.put(key(eventId, userId), eff);
                return;
            }

            // 2) Si ya hay uno en map y vino de EVENT, no dejes que GLOBAL lo pise
            // (si scopeEventId == null => este row es global)
            if (current != null && scopeEventId == null) return;


            // 3) Si no hay scope efectivo => sin permisos (si todavía no existe)
            if (effectiveScopeId == null) {
                if (current == null) {
                    var eff = new EffectivePerm(
                            eventId, eventName, startDatetime,paymentMode,timeZone, qrEnabled,
                            false, false, Collections.emptySet()
                    );
                    map.put(eventId, eff);
                    cache.put(key(eventId, userId), eff);
                }
                return;
            }
            Set<String> modules = null;
            if (!isAdmin) modules = loadModulesByScope(effectiveScopeId);

            // 4) Cargar módulos del scope (aquí sigue siendo 1 query por evento con scope)
            var eff = new EffectivePerm(
                    eventId, eventName, startDatetime,paymentMode, timeZone,qrEnabled,
                    false,      // isOwner
                    isAdmin,    // ✅ viene de la query principal
                    modules
            );
            map.put(eventId, eff);
            cache.put(key(eventId, userId), eff);
        });

        return map;
    }

    private Set<String> loadModulesByScope(long scopeId) {

        final String sql = """
        SELECT DISTINCT p.module_code
        FROM tbl_collaborator_scope_role csr
        JOIN tbl_role_permission rp ON rp.role_id = csr.role_id
        JOIN tbl_permission p       ON p.id = rp.permission_id
        WHERE csr.scope_id = :scopeId
        """;

        var params = new MapSqlParameterSource()
                .addValue("scopeId", scopeId);

        Set<String> modules = new HashSet<>();

        jdbc.query(sql, params, rs -> {
            modules.add(rs.getString("module_code"));
        });

        return modules;
    }
    private record Row(
        long eventId,
        String eventName,
        String paymentMode,
        String startDatetime,
        String timeZone,
        boolean qrEnabled,
        boolean isOwner,
        boolean isAdmin,
        Long effectiveScopeId
    ) {}
}

