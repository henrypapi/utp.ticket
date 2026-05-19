package com.pe.limon.api.transactions.tickets.repository;

import com.pe.limon.api.core.utils.enums.StatusEnum;
import com.pe.limon.api.gateway.admin.access.dto.AccessPassFiltersDTO;
import com.pe.limon.api.gateway.admin.events.dto.PageResult;
import com.pe.limon.api.transactions.orders.repository.entity.OrderItemEntity;
import com.pe.limon.api.transactions.tickets.repository.entity.AccessPassEntity;

import com.pe.limon.api.transactions.authz.repository.entity.PersonalInfoEntity;
import com.pe.limon.api.transactions.authz.repository.entity.UserEntity;

import com.pe.limon.api.transactions.events.repository.event.EventEntity;
import com.pe.limon.api.transactions.tickets.repository.entity.TicketEntity;
import com.pe.limon.api.transactions.tickets.repository.entity.TicketType;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ControlAccessRepository {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;
    @Value("${application.images.url.image-event}")
    private String urlBaseImage;
    @Value("${application.images.url.image-qr}")
    private String urlQRBaseImage;

    private final RowMapper<AccessPassEntity> accessPassRowMapper = (rs, rowNum) -> {
        AccessPassEntity entity = new AccessPassEntity();
        entity.setId(rs.getLong("id"));
        // Map access pass fields
        entity.setTicketId(rs.getLong("ticket_id"));
        entity.setCode(rs.getString("code"));
        entity.setAssignedUserId(rs.getString("assigned_user_id"));
        entity.setAssignedBy(rs.getString("assigned_by"));
        entity.setAssignedDatetime(rs.getTimestamp("assigned_datetime") != null ? rs.getTimestamp("assigned_datetime").toLocalDateTime() : null);
        entity.setAssignedTimestamp(rs.getLong("assigned_timestamp"));
        entity.setUsedDatetime(rs.getTimestamp("used_datetime") != null ? rs.getTimestamp("used_datetime").toLocalDateTime() : null);
        entity.setUsedTimestamp(rs.getLong("used_timestamp"));
        entity.setStatus(rs.getString("status"));
        entity.setAdmissionStatus(rs.getString("admission_status"));

        // Map ticket
        TicketEntity ticket = new TicketEntity();
        ticket.setTicketTypeId(rs.getLong("t_ticket_type_id"));

        // Map ticket type
        TicketType ticketType = new TicketType();
        ticketType.setId(rs.getLong("ticket_type_id"));
        ticketType.setName(rs.getString("ticket_type_name"));
        ticketType.setDescription(rs.getString("ticket_type_description"));
        ticketType.setEventId(rs.getLong("tt_event_id"));
        ticketType.setValidFromTimestamp(rs.getLong("tt_valid_from_timestamp"));
        ticketType.setValidUntilTimestamp(rs.getLong("tt_valid_until_timestamp"));
        ticketType.setEnableStartTimestamp(rs.getLong("tt_enable_start_timestamp"));
        ticketType.setEnableEndTimestamp(rs.getLong("tt_enable_end_timestamp"));
        ticket.setTicketType(ticketType);
        entity.setTicket(ticket);
        entity.setTicketTypeName(rs.getString("ticket_label"));

        UserEntity assignedUser = new UserEntity();
        assignedUser.setId(rs.getString("assigned_user_id"));
        assignedUser.setEmail(rs.getString("email"));

        PersonalInfoEntity personalInfo = new PersonalInfoEntity();
        personalInfo.setUserId(rs.getString("assigned_user_id"));
        personalInfo.setFirstName(rs.getString("first_name"));
        personalInfo.setLastName(rs.getString("last_name"));
        personalInfo.setEmail(rs.getString("email"));
        personalInfo.setPhoneNumber(rs.getString("phone_number"));
        personalInfo.setDocumentType(rs.getString("document_type"));
        personalInfo.setDocumentNumber(rs.getString("document_number"));
        assignedUser.setPersonalInfo(personalInfo);

        entity.setAssignedUser(assignedUser);
        return entity;
    };

    private final RowMapper<AccessPassEntity> accessPassListRowMapper = new RowMapper<>() {
        @Override
        public AccessPassEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
            AccessPassEntity entity = new AccessPassEntity();
            entity.setId(rs.getLong("access_pass_id"));
            // Map access pass fields
            entity.setTicketId(rs.getLong("ticket_id"));
            entity.setAssignedTimestamp(rs.getLong("assigned_timestamp"));
            if (rs.wasNull()) entity.setAssignedTimestamp(null);
            entity.setUsedDatetime(rs.getTimestamp("used_datetime") != null ? rs.getTimestamp("used_datetime").toLocalDateTime() : null);
            entity.setUsedTimestamp(rs.getLong("used_timestamp"));
            if (rs.wasNull()) entity.setUsedTimestamp(null);
            entity.setAdmissionStatus(rs.getString("admission_status"));
            entity.setTicketTypeName(rs.getString("ticket_type_name"));

            // Map ticket
            TicketEntity ticket = new TicketEntity();
            ticket.setId(rs.getLong("t_id"));
            ticket.setLabelName(rs.getString("t_label_name"));
            ticket.setTicketTypeId(rs.getLong("t_ticket_type_id"));

            // Map ticket type
            TicketType ticketType = new TicketType();
            ticketType.setId(rs.getLong("ticket_type_id"));
            ticketType.setEventId(rs.getLong("tt_event_id"));
            ticketType.setName(rs.getString("ticket_type_name"));
            ticketType.setDescription(rs.getString("tt_description"));
            ticket.setTicketType(ticketType);

            entity.setTicket(ticket);

            // Map assignedUser
            UserEntity assignedUser = new UserEntity();
            assignedUser.setId(rs.getString("u_id"));
            assignedUser.setEmail(rs.getString("u_email"));
            assignedUser.setStatus(rs.getString("u_status"));
            assignedUser.setPhoneNumber(rs.getString("phone_number"));


            // Map personalInfo
            PersonalInfoEntity personalInfo = new PersonalInfoEntity();
            personalInfo.setUserId(rs.getString("user_id"));
            personalInfo.setFirstName(rs.getString("first_name"));
            personalInfo.setLastName(rs.getString("last_name"));
            personalInfo.setEmail(rs.getString("email"));
            personalInfo.setPhoneNumber(rs.getString("phone_number"));
            personalInfo.setDocumentType(rs.getString("document_type"));
            personalInfo.setDocumentNumber(rs.getString("document_number"));

            assignedUser.setPersonalInfo(personalInfo);

            entity.setAssignedUser(assignedUser);

            return entity;
        }
    };

    public PageResult<AccessPassEntity> getAccessPassesByEventId(Long eventId, AccessPassFiltersDTO filters, int page, int size) {
        StringBuilder sql = new StringBuilder("""
            SELECT
                ap.id as access_pass_id,
                ap.ticket_id,
                ap.assigned_timestamp,
                ap.used_datetime,
                ap.used_timestamp,
                ap.status,
                CASE WHEN ap.used_timestamp IS NOT NULL THEN 'A' ELSE 'P' END AS admission_status,
                tt.id AS ticket_type_id,
                tt.name AS ticket_type_name,
                pi.user_id,
                pi.first_name,
                pi.last_name,
                pi.email,
                pi.phone_number,
                pi.document_type,
                pi.document_number,
                t.id AS t_id,
                CONCAT(tt.name, ' # ', tn.ticket_number) AS t_label_name,
                t.ticket_type_id AS t_ticket_type_id,
                tt.event_id AS tt_event_id,
                tt.description AS tt_description,
                u.id AS u_id,
                u.email AS u_email,
                u.status AS u_status
            FROM tbl_access_pass ap
            JOIN tbl_ticket t ON ap.ticket_id = t.id
            JOIN tbl_ticket_type tt ON t.ticket_type_id = tt.id
            JOIN (
              SELECT
                t2.id AS ticket_id,
                ROW_NUMBER() OVER (
                  PARTITION BY t2.ticket_type_id
                  ORDER BY t2.id
                ) AS ticket_number
              FROM tbl_ticket t2
              JOIN tbl_ticket_type tt2 ON tt2.id = t2.ticket_type_id
              WHERE tt2.event_id = :eventId
            ) tn ON tn.ticket_id = t.id
            LEFT JOIN tbl_personal_info pi ON ap.assigned_user_id = pi.user_id
            LEFT JOIN tbl_user u ON ap.assigned_user_id = u.id
            WHERE tt.event_id = :eventId
            AND ap.status <> 'I'
            """);

        // Count query
        StringBuilder countSql = new StringBuilder("""
            SELECT COUNT(*)
            FROM tbl_access_pass ap
            JOIN tbl_ticket t ON ap.ticket_id = t.id
            JOIN tbl_ticket_type tt ON t.ticket_type_id = tt.id
            -- 🔥 Numeración SOLO por ticket (NO por access_pass)
            JOIN (
              SELECT
                t2.id AS ticket_id,
                ROW_NUMBER() OVER (
                  PARTITION BY t2.ticket_type_id
                  ORDER BY t2.id
                ) AS ticket_number
              FROM tbl_ticket t2
              JOIN tbl_ticket_type tt2 ON tt2.id = t2.ticket_type_id
              WHERE tt2.event_id = :eventId
            ) tn ON tn.ticket_id = t.id
            LEFT JOIN tbl_personal_info pi ON ap.assigned_user_id = pi.user_id
            LEFT JOIN tbl_user u ON ap.assigned_user_id = u.id
            WHERE tt.event_id = :eventId
            AND ap.status <> 'I'
            """);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("eventId", eventId);

        if (filters.firstname() != null && !filters.firstname().trim().isEmpty()) {
            sql.append(" AND pi.first_name LIKE :firstname");
            countSql.append(" AND pi.first_name LIKE :firstname");
            params.addValue("firstname", "%" + filters.firstname().trim() + "%");
        }
        if (filters.lastname() != null && !filters.lastname().trim().isEmpty()) {
            sql.append(" AND pi.last_name LIKE :lastname");
            countSql.append(" AND pi.last_name LIKE :lastname");
            params.addValue("lastname", "%" + filters.lastname().trim() + "%");
        }
        if (filters.username() != null && !filters.username().trim().isEmpty()) {
            sql.append(" AND ap.assigned_user_id LIKE :username");
            countSql.append(" AND ap.assigned_user_id LIKE :username");
            params.addValue("username", "%" + filters.username().trim() + "%");
        }
        if (filters.email() != null && !filters.email().trim().isEmpty()) {
            sql.append(" AND pi.email LIKE :email");
            countSql.append(" AND pi.email LIKE :email");
            params.addValue("email", "%" + filters.email().trim() + "%");
        }
        if (filters.admissionStatus() != null && !filters.admissionStatus().trim().isEmpty()) {
            if (StatusEnum.ACTIVE.getCode().equals(filters.admissionStatus().trim())) {
                sql.append(" AND ap.used_timestamp IS NOT NULL");
                countSql.append(" AND ap.used_timestamp IS NOT NULL");
            } else if ("P".equals(filters.admissionStatus().trim())) {
                sql.append(" AND ap.used_timestamp IS NULL");
                countSql.append(" AND ap.used_timestamp IS NULL");
            }
        }
        
        sql.append(" ORDER BY ap.ticket_id DESC LIMIT :limit OFFSET :offset");
        params.addValue("limit", size);
        params.addValue("offset", (page - 1) * size);

        List<AccessPassEntity> accessPasses = namedJdbcTemplate.query(sql.toString(), params, accessPassListRowMapper);

        MapSqlParameterSource countParams = new MapSqlParameterSource()
                .addValue("eventId", eventId);


        Long totalElements = namedJdbcTemplate.queryForObject(countSql.toString(), countParams, Long.class);

        return new PageResult<>(accessPasses, page, size, totalElements != null ? totalElements : 0L);
    }

    public AccessPassEntity findByCode(String code) {
        try {
            String sql = """
            SELECT
                ap.id,
                ap.ticket_id,
                ap.code,
                ap.assigned_user_id,
                ap.assigned_by,
                ap.assigned_datetime,
                ap.assigned_timestamp,
                ap.used_datetime,
                ap.used_timestamp,
                ap.status,
                ap.status AS access_status,
                CASE WHEN ap.used_timestamp IS NOT NULL THEN 'A' ELSE 'P' END AS admission_status,
                tt.name ticket_type_name,
                tt.id AS ticket_type_id,
                t.ticket_type_id AS t_ticket_type_id,
                tt.description as ticket_type_description,
                tt.event_id AS tt_event_id,
                tt.active AS tt_active,
                CONCAT(
                    tt.name, ' ',
                    (
                      SELECT COUNT(*)
                      FROM tbl_ticket t2
                      WHERE t2.ticket_type_id = t.ticket_type_id
                        AND t2.id <= t.id
                    )
               ) AS ticket_label,
                tt.valid_from_timestamp AS tt_valid_from_timestamp,
                tt.valid_until_timestamp AS tt_valid_until_timestamp,
                tt.enable_start_timestamp AS tt_enable_start_timestamp,
                tt.enable_end_timestamp AS tt_enable_end_timestamp,
                pi.first_name,
                pi.last_name,
                pi.last_name,
                pi.document_number,
                pi.document_type,
                pi.email,
                pi.phone_number
            FROM tbl_access_pass ap
            INNER JOIN tbl_ticket t ON ap.ticket_id = t.id
            INNER JOIN tbl_ticket_type tt ON t.ticket_type_id = tt.id
            LEFT JOIN tbl_personal_info pi ON pi.user_id = ap.assigned_user_id
            WHERE ap.code = :code
            """;

            var params = new MapSqlParameterSource().addValue("code", code);

            return namedJdbcTemplate.queryForObject(sql, params, accessPassRowMapper);
        }catch (EmptyResultDataAccessException e){
            return null;
        }

    }

    public AccessPassEntity findById(Long code) {
        String sql = """
            SELECT
                ap.id,
                ap.ticket_id,
                ap.code,
                ap.assigned_user_id,
                ap.assigned_by,
                ap.assigned_datetime,
                ap.assigned_timestamp,
                ap.used_datetime,
                ap.used_timestamp,
                ap.status,
                ap.status AS access_status,
                CASE WHEN ap.used_timestamp IS NOT NULL THEN 'A' ELSE 'P' END AS admission_status,
                tt.name as ticket_type_name,
                CONCAT(
                    tt.name, ' ',
                    (
                      SELECT COUNT(*)
                      FROM tbl_ticket t2
                      WHERE t2.ticket_type_id = t.ticket_type_id
                        AND t2.id <= t.id
                    )
               ) AS ticket_label,
                tt.description as ticket_type_description,
                tt.id AS ticket_type_id,
                t.ticket_type_id AS t_ticket_type_id,
                tt.event_id AS tt_event_id,
                tt.active AS tt_active,
                tt.valid_from_timestamp AS tt_valid_from_timestamp,
                tt.valid_until_timestamp AS tt_valid_until_timestamp,
                tt.enable_start_timestamp AS tt_enable_start_timestamp,
                tt.enable_end_timestamp AS tt_enable_end_timestamp,
                pi.first_name,
                pi.last_name,
                pi.last_name,
                pi.document_number,
                pi.document_type,
                pi.email,
                pi.phone_number
            FROM tbl_access_pass ap
            INNER JOIN tbl_ticket t ON ap.ticket_id = t.id
            INNER JOIN tbl_ticket_type tt ON t.ticket_type_id = tt.id
            LEFT JOIN tbl_personal_info pi ON pi.user_id = ap.assigned_user_id
            WHERE ap.id = :id
            """;

        var params = new MapSqlParameterSource().addValue("id", code);

        return namedJdbcTemplate.queryForObject(sql, params, accessPassRowMapper);
    }

    public List<AccessPassEntity> findByTicketId(Long ticketId) {
        String sql = """
            SELECT
                id,
                ticket_id,
                code,
                assigned_user_id,
                assigned_by,
                assigned_datetime,
                assigned_timestamp,
                used_datetime,
                used_timestamp,
                status
            FROM tbl_access_pass
            WHERE ticket_id = :ticketId
            """;

        var params = new MapSqlParameterSource().addValue("ticketId", ticketId);

        return namedJdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(AccessPassEntity.class));
    }

    public int updateAccessPassForAdmission(Long id, Long usedTimestamp) {
        String sql = """
            UPDATE tbl_access_pass
            SET used_timestamp = :usedTimestamp, status = 'U'
            WHERE id = :id AND used_timestamp IS NULL AND status = 'A'
            """;
        var params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("usedTimestamp", usedTimestamp);
        return namedJdbcTemplate.update(sql, params);
    }

    public int[] batchInsert(List<AccessPassEntity> entities) {
        String sql = """
        INSERT INTO tbl_access_pass (
            ticket_id,
            code,
            assigned_user_id,
            assigned_by,
            assigned_datetime,
            assigned_timestamp,
            used_datetime,
            used_timestamp,
            status
        ) VALUES (
            :ticketId,
            :code,
            :assignedUserId,
            :assignedBy,
            :assignedDatetime,
            :assignedTimestamp,
            :usedDatetime,
            :usedTimestamp,
            :status
        )
    """;

        SqlParameterSource[] batchParams = entities.stream()
                .map(BeanPropertySqlParameterSource::new)
                .toArray(SqlParameterSource[]::new);

        return namedJdbcTemplate.batchUpdate(sql, batchParams);
    }

    public int updateCodeAndStatusByTicketId(AccessPassEntity accessPass) {
        String sql = """
            UPDATE tbl_access_pass
               SET code   = :code,
                   status = :status
             WHERE ticket_id = :ticketId AND id = :id
        """;

        BeanPropertySqlParameterSource params = new BeanPropertySqlParameterSource(accessPass);
        return namedJdbcTemplate.update(sql, params);
    }

    public List<AccessPassEntity> findByAssignedUserIdAndTicketId(String assignedUserId, Long orderId) {
        String sql = """
            SELECT
                ap.id,
                ap.ticket_id,
                ap.assigned_timestamp,
                ap.used_datetime,
                ap.used_timestamp,
                IF(ap.used_timestamp IS NOT NULL, 'A', 'P') AS admission_status,
                tt.id AS ticket_type_id,
                tt.name AS ticket_type_name,
                t.id AS t_id,
                t.label_name AS t_label_name,
                t.ticket_type_id AS t_ticket_type_id,
                tt.event_id AS tt_event_id,
                tt.active AS tt_active,
                tt.valid_from_timestamp AS tt_valid_from_timestamp,
                tt.valid_until_timestamp AS tt_valid_until_timestamp,
                tt.enable_start_timestamp AS tt_enable_start_timestamp,
                tt.enable_end_timestamp AS tt_enable_end_timestamp,
                 oi.order_id AS order_id,
                CONCAT(?, e.img) AS img,
                CONCAT(?, 'ticket-',ap.id, '.png') AS imgQR,
                e.name,
                e.address
            FROM tbl_access_pass ap
            INNER JOIN tbl_ticket t ON ap.ticket_id = t.id
            INNER JOIN tbl_ticket_type tt ON t.ticket_type_id = tt.id
            INNER JOIN tbl_event e ON tt.event_id = e.id
            INNER JOIN tbl_order_item oi ON oi.id = t.order_item_id
            WHERE ap.assigned_user_id = ?
            AND ap.status = 'A'
            AND oi.order_id = ?
            """;

        return jdbcTemplate.query(sql,  (rs, rowNum) -> {
            AccessPassEntity entity = new AccessPassEntity();
            entity.setId(rs.getLong("id"));
            entity.setTicketId(rs.getLong("ticket_id"));
            if (rs.wasNull()) entity.setAssignedTimestamp(null);
            entity.setUsedDatetime(rs.getTimestamp("used_datetime") != null ? rs.getTimestamp("used_datetime").toLocalDateTime() : null);
            entity.setUsedTimestamp(rs.getLong("used_timestamp"));
            if (rs.wasNull()) entity.setUsedTimestamp(null);
            entity.setAdmissionStatus(rs.getString("admission_status"));
            entity.setTicketTypeName(rs.getString("ticket_type_name"));
            entity.setUrl(rs.getString("imgQR"));
            // Map ticket
            TicketEntity ticket = new TicketEntity();
            OrderItemEntity orderItem = new OrderItemEntity();
            orderItem.setOrderId(rs.getLong("order_id"));
            ticket.setOrderItem(orderItem);
            ticket.setId(rs.getLong("t_id"));
            ticket.setLabelName(rs.getString("t_label_name"));
            ticket.setTicketTypeId(rs.getLong("t_ticket_type_id"));
            // Map ticket type
            TicketType ticketType = new TicketType();
            ticketType.setId(rs.getLong("ticket_type_id"));
            ticketType.setEventId(rs.getLong("tt_event_id"));
            ticket.setTicketType(ticketType);

            EventEntity eventEntity = new EventEntity();
            eventEntity.setImg(rs.getString("img"));
            eventEntity.setName(rs.getString("name"));
            eventEntity.setAddress("address");
            ticketType.setEvent(eventEntity);
            entity.setTicket(ticket);
            return entity;
        },urlBaseImage,urlQRBaseImage,assignedUserId, orderId);
    }

    public AccessPassEntity findById(String assignedUserId, Long accessPassId) {
        String sql = """
            SELECT
                ap.id,
                ap.ticket_id,
                ap.assigned_timestamp,
                ap.used_datetime,
                ap.used_timestamp,
                IF(ap.used_timestamp IS NOT NULL, 'A', 'P') AS admission_status,
                tt.id AS ticket_type_id,
                tt.name AS ticket_type_name,
                t.id AS t_id,
                t.label_name AS t_label_name,
                t.ticket_type_id AS t_ticket_type_id,
                tt.event_id AS tt_event_id,
                tt.active AS tt_active,
                tt.valid_from_timestamp AS tt_valid_from_timestamp,
                tt.valid_until_timestamp AS tt_valid_until_timestamp,
                tt.enable_start_timestamp AS tt_enable_start_timestamp,
                tt.enable_end_timestamp AS tt_enable_end_timestamp,
                 oi.order_id AS order_id,
                CONCAT(?, e.img) AS img,
                CONCAT(?, ap.id) AS imgQR,
                e.name,
                e.address
            FROM tbl_access_pass ap
            INNER JOIN tbl_ticket t ON ap.ticket_id = t.id
            INNER JOIN tbl_ticket_type tt ON t.ticket_type_id = tt.id
            INNER JOIN tbl_event e ON tt.event_id = e.id
            INNER JOIN tbl_order_item oi ON oi.id = t.order_item_id
            WHERE ap.assigned_user_id = ?
            AND ap.status = 'A'
            AND ap.id = ?
            """;

        return jdbcTemplate.queryForObject(sql,  (rs, rowNum) -> {
            AccessPassEntity entity = new AccessPassEntity();
            entity.setId(rs.getLong("id"));
            entity.setTicketId(rs.getLong("ticket_id"));
            if (rs.wasNull()) entity.setAssignedTimestamp(null);
            entity.setUsedDatetime(rs.getTimestamp("used_datetime") != null ? rs.getTimestamp("used_datetime").toLocalDateTime() : null);
            entity.setUsedTimestamp(rs.getLong("used_timestamp"));
            if (rs.wasNull()) entity.setUsedTimestamp(null);
            entity.setAdmissionStatus(rs.getString("admission_status"));
            entity.setTicketTypeName(rs.getString("ticket_type_name"));
            entity.setUrl(rs.getString("imgQR"));
            // Map ticket
            TicketEntity ticket = new TicketEntity();
            OrderItemEntity orderItem = new OrderItemEntity();
            orderItem.setOrderId(rs.getLong("order_id"));
            ticket.setOrderItem(orderItem);
            ticket.setId(rs.getLong("t_id"));
            ticket.setLabelName(rs.getString("t_label_name"));
            ticket.setTicketTypeId(rs.getLong("t_ticket_type_id"));
            // Map ticket type
            TicketType ticketType = new TicketType();
            ticketType.setId(rs.getLong("ticket_type_id"));
            ticketType.setEventId(rs.getLong("tt_event_id"));
            ticket.setTicketType(ticketType);

            EventEntity eventEntity = new EventEntity();
            eventEntity.setImg(rs.getString("img"));
            eventEntity.setName(rs.getString("name"));
            eventEntity.setAddress("address");
            ticketType.setEvent(eventEntity);
            entity.setTicket(ticket);
            return entity;
        },urlBaseImage,urlQRBaseImage,assignedUserId, accessPassId);
    }

}

