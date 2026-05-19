package com.pe.limon.api.transactions.tickets.repository;

import com.pe.limon.api.transactions.tickets.repository.entity.TicketType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class TicketTypeRepository {

    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedParameterJdbc;

    public Optional<TicketType> findById(long id) {
        String query = "SELECT * FROM tbl_ticket_type WHERE id = ?";
        try {
            TicketType ticketType =  jdbc.queryForObject(query,new BeanPropertyRowMapper<>(TicketType.class),id);
            return Optional.ofNullable(ticketType);
        }catch (EmptyResultDataAccessException e){
            return Optional.empty();
        }
    }

    public List<TicketType> findByEventIdAndEnableAndBetweenEnableDate(long eventId, boolean enable, long now) {
        String query = """   
        SELECT
            *
        FROM tbl_ticket_type
        WHERE
            event_id = ?
        AND active = ?
        AND ? BETWEEN enable_start_timestamp AND enable_end_timestamp
        """;
        return jdbc.query(query, new BeanPropertyRowMapper<>(TicketType.class), eventId, enable, now);
    }


    public List<TicketType> findByAndEventIdAndIdsEnableTimestampAndActive(
            long eventId,
            boolean enable,
            long now,
            List<Long> ids
    ) {
        String sql = """
        SELECT *
        FROM tbl_ticket_type
        WHERE event_id = :eventId
          AND active = :active
          AND :now BETWEEN enable_start_timestamp AND enable_end_timestamp
          AND id IN (:ids)
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("eventId", eventId)
                .addValue("active", enable)
                .addValue("now", now)
                .addValue("ids", ids);

        return namedParameterJdbc.query(sql, params, new BeanPropertyRowMapper<>(TicketType.class));
    }

    public List<TicketType> findByEventIdAndOwnerUserId(long eventId) {
        String query = """
               SELECT
            -- columnas de tbl_ticket_type
            tt.id,
            tt.event_id,
            tt.name,
            tt.description,
            tt.stock,
            tt.price,
            tt.seats_per_group,
            tt.purchase_quantity_limit,
            tt.active,
            tt.valid_from_timestamp,
            tt.valid_until_timestamp,
            tt.enable_start_timestamp,
            tt.enable_end_timestamp,
            tt.registered_timestamp,
            tt.registered_by,
            tt.sold,
            (tt.stock - tt.sold - tt.reserved) AS available_tickets
        FROM tbl_ticket_type tt
        WHERE tt.event_id = ?
        """;
        return jdbc.query(query, new BeanPropertyRowMapper<>(TicketType.class), eventId);
    }

    public TicketType findByIdAndOwnerUserId(long id, String ownerUserId) {
        try {
            String query = """
               SELECT
                   tt.*
               FROM
               tbl_ticket_type tt
               INNER JOIN
               tbl_event e ON tt.event_id = e.id
               WHERE e.owner_user_id = ?
               AND tt.id = ? LIMIT 1
           """;
            return jdbc.queryForObject(query, new BeanPropertyRowMapper<>(TicketType.class), ownerUserId, id);
        }catch (EmptyResultDataAccessException e){
            return null;
        }
    }

    public void save(TicketType ticketType) {
        String query = """
            INSERT INTO tbl_ticket_type (
             event_id, name, description, price, stock, reserved, sold, registered_timestamp, registered_datetime,
             active,enable_start_datetime, enable_start_timestamp, enable_end_datetime, enable_end_timestamp,
             valid_from_datetime, valid_from_timestamp, valid_until_datetime, valid_until_timestamp, registered_by,
             purchase_quantity_limit, seats_per_group
           ) VALUES (
             :eventId,:name,:description,:price,:stock,:reserved,:sold,:registeredTimestamp,:registeredDatetime,
             :active, :enableStartDatetime, :enableStartTimestamp, :enableEndDatetime, :enableEndTimestamp,
             :validFromDatetime, :validFromTimestamp, :validUntilDatetime, :validUntilTimestamp, :registeredBy,
             :purchaseQuantityLimit, :seatsPerGroup
           )
           """;
        namedParameterJdbc.update(query, new BeanPropertySqlParameterSource(ticketType));
    }

    public void update(TicketType ticketType) {
        String query = """
                UPDATE tbl_ticket_type
                SET
                    name = :name,
                    description = :description,
                    price = :price,
                    stock = :stock,
                    active = :active,
                    enable_start_datetime = :enableStartDatetime,
                    enable_end_datetime = :enableEndDatetime,
                    valid_from_datetime = :validFromDatetime,
                    valid_until_datetime = :validUntilDatetime,
                    enable_start_timestamp = :enableStartTimestamp,
                    enable_end_timestamp = :enableEndTimestamp,
                    valid_from_timestamp = :validFromTimestamp,
                    valid_until_timestamp = :validUntilTimestamp,
                    purchase_quantity_limit = :purchaseQuantityLimit,
                    seats_per_group = :seatsPerGroup
                WHERE event_id = :eventId
                AND id = :id
            """;
        namedParameterJdbc.update(query, new BeanPropertySqlParameterSource(ticketType));
    }


    public void updateValidTicket(TicketType ticketType) {
        String query = """
                UPDATE tbl_ticket_type
                SET
                    valid_from_datetime = :validFromDatetime,
                    valid_until_datetime = :validUntilDatetime,
                    valid_from_timestamp = :validFromTimestamp,
                    valid_until_timestamp = :validUntilTimestamp
                WHERE event_id = :eventId
                AND id = :id
            """;
        namedParameterJdbc.update(query, new BeanPropertySqlParameterSource(ticketType));
    }

    public int reserveStock(Long ticketTypeId, int qty) {
        String sql = """
          UPDATE tbl_ticket_type
             SET reserved = reserved + ?
           WHERE id = ?
             AND (stock - reserved - sold) >= ?;
        """;
        return jdbc.update(sql, qty, ticketTypeId, qty);
    }

    public int confirmStock(Long ticketTypeId, int qty) {
        log.info("[confirmStock] Starting ticketTypeId {} - qty {}:", ticketTypeId , qty);
        String sql = """
          UPDATE tbl_ticket_type
             SET reserved = reserved - ?,
                 sold     = sold + ?
           WHERE id = ?
             AND reserved >= ?;
        """;
        return jdbc.update(sql, qty, qty, ticketTypeId, qty);
    }

    public int releaseStock(Long ticketTypeId, int qty) {
        String sql = """
          UPDATE tbl_ticket_type
             SET reserved = reserved - ?
           WHERE id = ?
             AND reserved >= ?;
        """;
        return jdbc.update(sql, qty, ticketTypeId, qty);
    }
}
