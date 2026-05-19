package com.pe.limon.api.transactions.authz.repository;

import com.pe.limon.api.core.utils.conversor.JsonUtils;
import com.pe.limon.api.transactions.authz.repository.entity.*;
import com.pe.limon.api.gateway.admin.events.dto.PageResult;
import com.pe.limon.api.transactions.tickets.repository.entity.AccessPassEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CollaboratorRepository {

    private final NamedParameterJdbcTemplate namedJdbc;

    public PageResult<CollaboratorRow> findByFilters(String ownerUserId, String username, int page, int size) {
        StringBuilder sql = new StringBuilder("""
        SELECT
            c.id,
            c.user_id,
            c.owner_user_id,
            p.username
        FROM tbl_collaborator c
        inner JOIN tbl_profile p ON p.user_id = c.user_id
        WHERE c.owner_user_id = :ownerUserId
    """);

        StringBuilder countSql = new StringBuilder("""
        SELECT COUNT(*)
        FROM tbl_collaborator c
        inner JOIN tbl_profile p ON p.user_id = c.user_id
        WHERE c.owner_user_id = :ownerUserId
    """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("ownerUserId", ownerUserId);

        if (username != null && !username.isBlank()) {
            sql.append(" AND p.username = :username");
            countSql.append(" AND p.username = :username");
            params.addValue("username", username);
        }

        sql.append(" ORDER BY  c.id DESC LIMIT :limit OFFSET :offset");

        int offset = (page - 1) * size;
        params.addValue("limit", size);
        params.addValue("offset", offset);

        List<CollaboratorRow> list = namedJdbc.query(sql.toString(), params, (rs, rowNum) ->
                new CollaboratorRow(
                        rs.getLong("id"),
                        rs.getString("user_id"),
                        rs.getString("owner_user_id"),
                        rs.getString("username"),
                        "",
                        0,
                        "",
                        0
                )
        );

        Long total = namedJdbc.queryForObject(countSql.toString(), params, Long.class);
        return new PageResult<>(list, page, size, total);
    }


    public List<ScopeRoleEntity> findById(CollaboratorEntity entity){
        try {
            String sql = """
                SELECT cs.event_id_key, csr.role_id, scope_type
                FROM tbl_collaborator c
                LEFT JOIN  tbl_collaborator_scope cs
                ON cs.collaborator_id = c.id
                LEFT JOIN  tbl_collaborator_scope_role csr
                ON csr.scope_id = cs.id
                WHERE c.id = :id AND c.owner_user_id = :ownerUserId
            """;
            return namedJdbc.query(sql,new BeanPropertySqlParameterSource(entity), new BeanPropertyRowMapper<>(ScopeRoleEntity.class));
        }catch (EmptyResultDataAccessException e){
            return null;
        }
    }

    public Long insert(CollaboratorEntity entity) {
        String sql = """
        INSERT INTO tbl_collaborator (
            user_id,
            owner_user_id,
            registered_datetime,
            registered_timestamp
        ) VALUES (
            :userId,
            :ownerUserId,
            :registeredDatetime,
            :registeredTimestamp
        );
    """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedJdbc.update(sql, new BeanPropertySqlParameterSource(entity), keyHolder, new String[]{"id"});

        Number key = keyHolder.getKey();
        return (key != null) ? key.longValue() : null;
    }

    public int[] batchScopeRolesInsert(List<ScopeRoleEntity> entities) {
        String sql = """
        INSERT INTO tbl_collaborator_scope_role (
            scope_id, role_id, registered_datetime, registered_timestamp
        ) VALUES (
            :scopeId, :roleId, :registeredDatetime, :registeredTimestamp
        )
    """;

        SqlParameterSource[] batchParams = entities.stream()
                .map(BeanPropertySqlParameterSource::new)
                .toArray(SqlParameterSource[]::new);

        return namedJdbc.batchUpdate(sql, batchParams);
    }

    public Long insertScope(ScopeEntity entity) {
        String sql = """
        INSERT INTO tbl_collaborator_scope (
            collaborator_id,scope_type, event_id, registered_datetime, registered_timestamp
        ) VALUES (
            :collaboratorId,:scopeType, :eventId, :registeredDatetime, :registeredTimestamp
        )
    """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedJdbc.update(sql, new BeanPropertySqlParameterSource(entity), keyHolder, new String[]{"id"});

        Number key = keyHolder.getKey();
        return (key != null) ? key.longValue() : null;
    }

    /**
     * Elimina todos los registros relacionados en tbl_event_collaborator
     * vinculados a un colaborador específico.
     *
     * @param collaboratorId ID del colaborador
     */
    public int deleteScopeRoleByIdAndRoles(List<Long> scopeById, List<Long> roles) {
        final String sql = """
                DELETE FROM tbl_collaborator_scope_role
                WHERE scope_id IN (:scopeById) AND role_id IN (:roles)
        """;

        var params = new MapSqlParameterSource()
                .addValue("scopeById", scopeById)
                .addValue("roles", roles);

        return namedJdbc.update(sql, params);
    }


    public int deleteScopeByIds(List<Long> scopeIds) {
        final String sql = """
            DELETE FROM tbl_collaborator_scope
            WHERE id IN (:scopeIds)
        """;
        var params = new MapSqlParameterSource().addValue("scopeIds", scopeIds);
        return namedJdbc.update(sql, params);
    }


    public int deleteCollaboratorByIdAndOwnerUserId(long collaboratorId, String ownerUserId ) {

        final String sql = """
                DELETE
                FROM tbl_collaborator c
                WHERE c.id = :collaboratorId
                  AND c.owner_user_id = :ownerUserId
        """;

        var params = new MapSqlParameterSource()
                .addValue("collaboratorId", collaboratorId)
                .addValue("ownerUserId", ownerUserId);

        return namedJdbc.update(sql, params);
    }


    public List<CollaboratorScopeEntity> findScopeIdsByCollabIdAndOwnerUserId(long collaboratorId, String ownerUserId) {

        final String sql = """
                Select sc.*
                FROM tbl_collaborator_scope sc
                INNER JOIN tbl_collaborator c ON c.id = sc.collaborator_id
                WHERE c.id = :collaboratorId
                  AND c.owner_user_id = :ownerUserId
        """;

        var params = new MapSqlParameterSource()
                .addValue("collaboratorId", collaboratorId)
                .addValue("ownerUserId", ownerUserId);

        return namedJdbc.query(sql,params, new BeanPropertyRowMapper<>(CollaboratorScopeEntity.class));
    }

}
