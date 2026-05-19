package com.pe.limon.api.transactions.orders.repository;

import com.pe.limon.api.gateway.admin.events.dto.PageResult;
import com.pe.limon.api.transactions.orders.repository.entity.OrderEntity;
import lombok.RequiredArgsConstructor;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public Long insert(OrderEntity order) {
        String sql = """
            INSERT INTO tbl_order (
                user_id,
                event_id,
                status,
                total_quantity,
                total_amount,
                registered_datetime,
                updated_datetime,
                registered_timestamp,
                updated_timestamp,
                payment_reference,
                commission_amount,
                commission_percent,
                net_amount
            )
            VALUES (
                :userId,
                :eventId,
                :status,
                :totalQuantity,
                :totalAmount,
                :registeredDatetime,
                :updatedDatetime,
                :registeredTimestamp,
                :updatedTimestamp,
                :paymentReference,
                :commissionAmount,
                :commissionPercent,
                :netAmount
            )
            """;


        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedJdbcTemplate.update(sql, new BeanPropertySqlParameterSource(order), keyHolder, new String[]{"id"});

        Number key = keyHolder.getKey();
        Long generatedId = (key != null) ? key.longValue() : null;
        order.setId(generatedId);
        return generatedId;
    }

    public PageResult<OrderEntity> getSalesByEventId(Long eventId, String orderStatus, int page, int size) {
        if (orderStatus == null || orderStatus.isEmpty()) {
            orderStatus = null;
        }
        String sql = """
            SELECT
                o.id,
                o.user_id,
                o.event_id,
                o.status,
                o.total_quantity,
                o.total_amount,
                o.registered_datetime,
                o.updated_datetime,
                o.registered_timestamp,
                o.updated_timestamp,
                o.payment_reference
            FROM tbl_order o
            WHERE o.event_id = :eventId AND (:orderStatus IS NULL OR status = :orderStatus)
            ORDER BY o.registered_timestamp DESC
            LIMIT :limit OFFSET :offset
            """;

        String countSql = """
            SELECT COUNT(*) FROM tbl_order o WHERE o.event_id = :eventId
            """;

        var params = new MapSqlParameterSource()
                .addValue("orderStatus", orderStatus)
                .addValue("eventId", eventId)
                .addValue("limit", size)
                .addValue("offset", (page-1) * size);

        List<OrderEntity> orders = namedJdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(OrderEntity.class));


        Long totalElements = namedJdbcTemplate.queryForObject(countSql,
                new MapSqlParameterSource().addValue("eventId", eventId), Long.class);

        return new PageResult<>(orders, page, size, totalElements != null ? totalElements : 0L);
    }

    public OrderEntity findById(Long orderId) {
        String sql = """
            SELECT
                *
            FROM tbl_order o
            WHERE o.id = :orderId
            """;

        var params = new MapSqlParameterSource().addValue("orderId", orderId);

        return namedJdbcTemplate.queryForObject(sql, params, new BeanPropertyRowMapper<>(OrderEntity.class));
    }

    public OrderEntity findByIdAndUserId(Long orderId, String userId) {
        String sql = """
            SELECT
                *
            FROM tbl_order o
            WHERE o.id = :orderId and o.user_id = :userId
            """;

        var params = new MapSqlParameterSource().addValue("orderId", orderId).addValue("userId", userId);

        return namedJdbcTemplate.queryForObject(sql, params, new BeanPropertyRowMapper<>(OrderEntity.class));
    }

    public OrderEntity findByIdAndEventId(Long eventId,Long orderId) {
        try {
            String sql = """
            SELECT
                *
            FROM tbl_order o
            WHERE o.id = :orderId AND event_id = :eventId
            """;

            var params = new MapSqlParameterSource()
                    .addValue("orderId", orderId)
                    .addValue("eventId", eventId);

            return namedJdbcTemplate.queryForObject(sql, params, new BeanPropertyRowMapper<>(OrderEntity.class));
        }catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public boolean existsByEventIdAndOrderIdAndStatuses(Long eventId,Long orderId, List<String> statuses) {
        String sql = """
        SELECT
            count(*)
        FROM tbl_order o
        WHERE o.id = :orderId AND event_id = :eventId AND status IN (:statuses)
        """;

        var params = new MapSqlParameterSource()
                .addValue("orderId", orderId)
                .addValue("statuses", statuses)
                .addValue("eventId", eventId);

        var count = namedJdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count>0;
    }

    public long updateStatus(OrderEntity orderEntity) {
        String sql = """
            UPDATE tbl_order SET
                status = :status,
                updated_datetime = :updatedDatetime,
                updated_timestamp= :updatedTimestamp,
                updated_by = :updatedBy,
                payment_reference = COALESCE(:paymentReference, payment_reference)
            WHERE id =:id AND event_id = :eventId
        """;
        return namedJdbcTemplate.update(sql, new BeanPropertySqlParameterSource(orderEntity));
    }
}
