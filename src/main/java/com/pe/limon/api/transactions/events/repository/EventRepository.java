package com.pe.limon.api.transactions.events.repository;


import com.pe.limon.api.gateway.admin.events.dto.PageResult;
import com.pe.limon.api.transactions.authz.repository.entity.ProfileEntity;
import com.pe.limon.api.transactions.authz.repository.entity.UserEntity;
import com.pe.limon.api.transactions.events.repository.event.EventEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class EventRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public void insert(EventEntity event){
        String sql = """
            INSERT INTO tbl_event (
                name, description, status,type_id, img, payment_mode,
                address, location, country, time_zone,
                start_datetime,registered_datetime, registered_timestamp, qr_enabled,owner_user_id,
                terms, metadata, payment_comment, redirect_after_pay
            ) VALUES (
                :name, :description, :status,:typeId, :img, :paymentMode,
                :address, ST_GeomFromText(:location), :country, :timeZone,
                :startDatetime,:registeredDatetime, :registeredTimestamp, :qrEnabled, :ownerUserId,
                :terms, :metadata, :paymentComment, :redirectAfterPay
            )
        """;

        BeanPropertySqlParameterSource params = new BeanPropertySqlParameterSource(event);
        namedParameterJdbcTemplate.update(sql, params);
    }

    public void update(EventEntity event){
        String sql = """
            UPDATE tbl_event
            SET
                name = :name,
                description = :description,
                type_id = :typeId,
                address = :address,
                location = ST_GeomFromText(:location),
                start_datetime = :startDatetime,
                qr_enabled = :qrEnabled,
                payment_mode = :paymentMode,
                terms = :terms,
                metadata = :metadata,
                redirect_after_pay = :redirectAfterPay,
                payment_comment = :paymentComment
            WHERE id = :id AND owner_user_id = :ownerUserId
        """;

        BeanPropertySqlParameterSource params = new BeanPropertySqlParameterSource(event);
        namedParameterJdbcTemplate.update(sql, params);
    }

    public EventEntity findById(Long eventId) {
        try {
            String sql = """
                SELECT
                  id,
                  name,
                  description,
                  type_id,
                  img,
                  address,
                  payment_mode,
                  ST_AsText(location) AS location,
                  start_datetime,
                  registered_datetime,
                  time_zone,
                  qr_enabled,
                  owner_user_id,
                  terms,
                status,
    redirect_after_pay, payment_comment
                FROM tbl_event e
                WHERE e.id = ?
            """;


            return jdbcTemplate.queryForObject(
                    sql,
                    new BeanPropertyRowMapper<>(EventEntity.class),
                    eventId
            );
        }catch (EmptyResultDataAccessException e){
            return null;
        }
    }


    public EventEntity findByIdAndStatus(Long eventId, String status) {
        try {
            String sql = """
                SELECT
                  id,
                  name,
                  description,
                  type_id,
                  img,
                  address,
                  payment_mode,
                  ST_AsText(location) AS location,
                  start_datetime,
                  registered_datetime,
                  time_zone,
                  qr_enabled,
                  owner_user_id,
                  terms
                FROM tbl_event e
                WHERE e.id = ? and e.status = ?
            """;


            return jdbcTemplate.queryForObject(
                    sql,
                    new BeanPropertyRowMapper<>(EventEntity.class),
                    eventId, status
            );
        }catch (EmptyResultDataAccessException e){
            return null;
        }
    }

    public List<EventEntity> findAll(LocalDateTime today) {
        log.debug("[findAll] Today : {}", today.toString());
        try {
            String sql = """
            SELECT
                e.id,
                e.name,
                e.start_datetime,
                img,
                owner_user_id,
                e.address,
                p.username,
                p.profile_image
            FROM tbl_event e
            INNER JOIN tbl_profile p
             ON   e.owner_user_id = p.user_id
            WHERE
                e.start_datetime >= ? and e.status = 'A'
        """;
            return jdbcTemplate.query(
                    sql,
                    (rs,rows)->{
                        EventEntity e = new EventEntity();
                        e.setId(rs.getLong("id"));
                        e.setName(rs.getString("name"));
                        e.setImg(rs.getString("img"));
                        e.setOwnerUserId(rs.getString("owner_user_id"));
                        e.setAddress(rs.getString("address"));
                        e.setStartDatetime(rs.getTimestamp("start_datetime").toLocalDateTime());
                        ProfileEntity profileEntity = new ProfileEntity();
                        profileEntity.setUserId(rs.getString("owner_user_id"));
                        profileEntity.setUsername(rs.getString("username"));
                        profileEntity.setProfileImage(rs.getString("profile_image"));
                        UserEntity user =  new UserEntity();
                        user.setProfile(profileEntity);
                        user.setId(rs.getString("owner_user_id"));
                        e.setOwnerUser(user);
                        return e;
                    },
                    today
            );
        }catch (EmptyResultDataAccessException e){
            return null;
        }
    }

    public boolean existsByIdAndOwnerUser(EventEntity event){
        String sql = "SELECT count(*) FROM tbl_event where id = ? and owner_user_id = ?;";
        Integer count = jdbcTemplate.queryForObject(sql,Integer.class, event.getId(),event.getOwnerUserId());
        return count != null && count > 0;
    }

    public EventEntity findByIdAndOwnerUser(Long eventId, String userId) {
        try {
            String sql = """
            SELECT
               id,
              name,
              description,
              type_id,
              img,
              address,
              ST_AsText(location) AS location,
              start_datetime,
              registered_datetime,
              qr_enabled,
              owner_user_id,
              terms
            FROM tbl_event
            WHERE owner_user_id = ?
            AND id = ?
            """;
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(EventEntity.class), userId, eventId);
        }catch (EmptyResultDataAccessException e){
            return null;
        }
    }

    public List<Long> findIdsByOwnerUserId(String ownerUserId){
        String sql = "SELECT id FROM tbl_event WHERE owner_user_id = ?;";
        return jdbcTemplate.queryForList(sql, Long.class, ownerUserId);
    }

    public PageResult<EventEntity> findByOwnerOrAdminUser(String userId, int page, int size) {
        String sql = """
           SELECT e.*
            FROM tbl_event e
            LEFT JOIN (
                SELECT DISTINCT
                  e2.id AS event_id
                FROM tbl_event e2
                JOIN tbl_collaborator c
                  ON c.owner_user_id = e2.owner_user_id
                 AND c.user_id = ?                       -- user logueado
                JOIN tbl_collaborator_scope s
                  ON s.collaborator_id = c.id
                 AND (
                      (s.scope_type = 'EVENT'  AND s.event_id = e2.id)
                   OR (s.scope_type = 'GLOBAL' AND s.event_id IS NULL)
                 )
                JOIN tbl_collaborator_scope_role csr
                  ON csr.scope_id = s.id
                JOIN tbl_role r
                  ON r.id = csr.role_id
                 AND r.name = 'ADMIN'
            ) perm
              ON perm.event_id = e.id
            WHERE e.owner_user_id = ? OR perm.event_id IS NOT NULL
        ORDER BY e.id DESC
        LIMIT ?
        OFFSET ?
        """;
        String countSql = """
        SELECT COUNT(*)
        FROM tbl_event e
            LEFT JOIN (
                SELECT DISTINCT
                  e2.id AS event_id
                FROM tbl_event e2
                JOIN tbl_collaborator c
                  ON c.owner_user_id = e2.owner_user_id
                 AND c.user_id = ?                       -- user logueado
                JOIN tbl_collaborator_scope s
                  ON s.collaborator_id = c.id
                 AND (
                      (s.scope_type = 'EVENT'  AND s.event_id = e2.id)
                   OR (s.scope_type = 'GLOBAL' AND s.event_id IS NULL)
                 )
                JOIN tbl_collaborator_scope_role csr
                  ON csr.scope_id = s.id
                JOIN tbl_role r
                  ON r.id = csr.role_id
                 AND r.name = 'ADMIN'
            ) perm
              ON perm.event_id = e.id
        WHERE e.owner_user_id = ? OR perm.event_id IS NOT NULL
      """;

        int offset = (page - 1) * size;
        List<EventEntity> events = jdbcTemplate.query(
                sql, new BeanPropertyRowMapper<>(EventEntity.class), userId, userId,size, offset
        );

        Long total = jdbcTemplate.queryForObject(countSql, Long.class, userId, userId);

        return new PageResult<>(events, page, size, total);
    }

    public EventEntity findByOwnerUserIdAndEventId(String userId, long eventId) {
        String sql = """
            SELECT
              id,
              name,
              description,
              type_id,
              img,
              address,
              ST_AsText(location) AS location,
              start_datetime,
              registered_datetime,
              time_zone,
              qr_enabled,
              owner_user_id,
              terms,
              metadata,
              payment_comment,
              redirect_after_pay,
              payment_mode
              FROM tbl_event
              WHERE owner_user_id = ? AND id = ?;
        """;

        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(EventEntity.class), userId, eventId);
    }
  public boolean isEventValidAndQrEnabled(Long eventId) {
        String sql = "SELECT qr_enabled FROM tbl_event WHERE id = :eventId";
        var params = new MapSqlParameterSource().addValue("eventId", eventId);
        Boolean qrEnabled = namedParameterJdbcTemplate.queryForObject(sql, params, Boolean.class);
        return qrEnabled != null && qrEnabled;
    }
}
