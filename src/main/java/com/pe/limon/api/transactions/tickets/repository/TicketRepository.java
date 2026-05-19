package com.pe.limon.api.transactions.tickets.repository;



import com.pe.limon.api.transactions.authz.repository.entity.UserEntity;
import com.pe.limon.api.transactions.events.repository.event.EventEntity;
import com.pe.limon.api.transactions.events.repository.event.EventTicket;
import com.pe.limon.api.transactions.orders.repository.entity.OrderEntity;
import com.pe.limon.api.transactions.orders.repository.entity.OrderItemEntity;
import com.pe.limon.api.transactions.tickets.repository.entity.AccessPassEntity;
import com.pe.limon.api.transactions.tickets.repository.entity.TicketEntity;
import com.pe.limon.api.transactions.tickets.repository.entity.TicketType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
@RequiredArgsConstructor
@Slf4j
public class TicketRepository {

    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    @Value("${application.images.url.image-event}")
    private String urlBaseImage;
    public Optional<EventTicket> findByIdAndOwnerUser(long id, String userId) {
        String query = """
            SELECT et.* FROM tbl_event_ticket et
            INNER JOIN tbl_event e ON e.id = et.event_id
            WHERE et.id = ? AND e.owner_user_id = ?
            """;
        try {
            EventTicket eventTicket =  jdbc.queryForObject(query,new BeanPropertyRowMapper<>(EventTicket.class),id, userId);
            log.debug("[findByIdAndOwnerUser] event ticket got : {} ", eventTicket);
            return Optional.of(eventTicket);
        }catch (EmptyResultDataAccessException e){
            return Optional.empty();
        }
    }

    public EventTicket save(EventTicket ticket) {
        String sql = """
            INSERT INTO tbl_eventt (ticket_id, token, status, used_at,created_at, event_id)
            VALUES ( ?, ?, ?, ?, ?, ?)
        """;

        jdbc.update(sql,
            ticket.getId(),
            ticket.getTicketTypeId(),
            ticket.getToken(),
            ticket.getStatus(),
            Timestamp.valueOf(ticket.getCreatedAt()),
            ticket.getEventId()
        );

        return ticket;
    }

    public Long insert(TicketEntity ticket) {
        String sql = """
            INSERT INTO tbl_ticket (label_name, order_item_id,registered_datetime, ticket_type_id,registered_timestamp,registered_by)
            VALUES (:labelName, :orderItemId,:registeredDatetime, :ticketTypeId,:registeredTimestamp,:registeredBy)
        """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedJdbcTemplate.update(sql, new BeanPropertySqlParameterSource(ticket), keyHolder, new String[]{"id"});

        Number key = keyHolder.getKey();
        Long generatedId = (key != null) ? key.longValue() : null;
        ticket.setId(generatedId);
        return generatedId;
    }

    public Optional<EventTicket> findByToken(String userId, EventTicket entity) {
        String sql = """
                SELECT
                    e.name AS eName,
                    e.qr_enabled,
                    a.ticket_type_id,
                    a.event_id,
                    u.name AS pName,
                    et.token As token,
                    et.expired_at,
                    et.used_at,
                    et.status
                FROM
                    tbl_event_participant a
                INNER JOIN
                    tbl_event e ON e.id = a.event_id
                INNER JOIN
                    tbl_event_ticket et ON et.id = a.event_ticket_id
                INNER JOIN
                    tbl_user u ON u.id = a.user_id
                WHERE
                    e.owner_user_id = ?
                    AND et.token = ?
                """;

        log.debug("[findByToken] params in {} - {}", entity.getEventId(),entity.getToken());
        try {
            EventTicket ticket = jdbc.queryForObject(sql, (rs, rowNum) -> {
                    EventTicket ticketEntity = new EventTicket();
                    ticketEntity.setStatus(rs.getString("status"));
                    ticketEntity.setTicketTypeId(rs.getLong("ticket_type_id"));
                    if (rs.getTimestamp("used_at")!=null){
                        ticketEntity.setUsedAt(rs.getTimestamp("used_at").toLocalDateTime());
                    }
                    ticketEntity.setToken(rs.getString("token"));
                    ticketEntity.setExpiredAt(rs.getTimestamp("expired_at").toLocalDateTime());


                    EventEntity event = new EventEntity();
                    event.setName(rs.getString("eName"));
                    event.setId(rs.getLong("event_id"));
                    event.setQrEnabled(rs.getBoolean("qr_enabled"));
                    ticketEntity.setEvent(event);
                    return ticketEntity;
                },
                userId,
                entity.getToken()
            );
            return Optional.of(ticket);
        } catch (EmptyResultDataAccessException e) {
            log.debug("[findByToken] Not found by {} ", entity);
            return Optional.empty();
        }
    }

    public Optional<AccessPassEntity> findByUserId(String userId, Long accessId) {
        String sql = """
                SELECT
                    *
                FROM
                    tbl_access_pass a
                WHERE
                    a.id  = ?
                    And a.assigned_user_id = ?
                """;

        log.debug("[findByToken] params in {} - {}", accessId, userId);
        try {
            AccessPassEntity ticket = jdbc.queryForObject(sql, new BeanPropertyRowMapper<>(AccessPassEntity.class),
                    accessId, userId
            );
            return Optional.of(ticket);
        } catch (EmptyResultDataAccessException e) {
            log.debug("[findByToken] Not found by {} - {}", accessId, userId);
            return Optional.empty();
        }
    }

    public void markAsUsed(Long ticketId, LocalDateTime usedAt) {
        log.info("[marksAsUsed] Init process {} - {}:",ticketId, usedAt);
        String sql = "UPDATE tbl_event_ticket SET used_at = ?, status = 'U' WHERE id = ?";
        jdbc.update(sql, Timestamp.valueOf(usedAt), ticketId);
    }

    public List<Long> findIdsByOrderId(Long orderId) {
        String sql = """
                SELECT distinct (t.id) AS id
                FROM
                    tbl_ticket t
                INNER JOIN
                    tbl_order_item oi ON oi.id = t.order_item_id
                INNER JOIN
                    tbl_order o ON o.id = oi.order_id
                WHERE
                    o.id  = ?
                """;

        return jdbc.queryForList(sql, Long.class, orderId);
    }

    public List<OrderEntity> findByAssignedUserId(String assignedUserId) {
        String sql = """
            SELECT
                o.id,
                o.event_id,
                CONCAT(?, e.img) AS img,
                e.name,
                e.address,
                e.qr_enabled,
    e.start_datetime,
                o.status
            FROM tbl_order o
            INNER JOIN tbl_event e ON o.event_id = e.id
            WHERE o.user_id = ? order by o.id desc
            """;

        return jdbc.query(sql,  (rs, rowNum) -> {
            // Map ticket
            OrderEntity order = new OrderEntity();
            order.setId(rs.getLong("id"));
            order.setStatus(rs.getString("status"));
            EventEntity eventEntity = new EventEntity();
            eventEntity.setId(rs.getLong("event_id"));
            eventEntity.setImg(rs.getString("img"));
            eventEntity.setName(rs.getString("name"));
            eventEntity.setQrEnabled(rs.getBoolean("qr_enabled"));
            eventEntity.setAddress(rs.getString("address"));
            eventEntity.setStartDatetime(rs.getTimestamp("start_datetime").toLocalDateTime());
            order.setEvent(eventEntity);
            return order;
        },urlBaseImage,assignedUserId);
    }


}
