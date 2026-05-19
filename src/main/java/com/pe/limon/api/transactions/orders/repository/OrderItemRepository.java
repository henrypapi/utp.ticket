package com.pe.limon.api.transactions.orders.repository;

import com.pe.limon.api.transactions.orders.repository.entity.OrderItemEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class OrderItemRepository {
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public List<OrderItemEntity> findByOrderId(Long orderId) {
        String sql = """
            SELECT
                oi.id,
                oi.quantity,
                oi.product_id,
                oi.product_type,
                oi.unit_price,
                oi.subtotal
            FROM tbl_order_item oi
            WHERE oi.order_id = :orderId
            """;

        var params = new MapSqlParameterSource().addValue("orderId", orderId);

        return namedJdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(OrderItemEntity.class));
    }

    public Map<Long, Integer> findQuantityByProductId(Long eventId) {

        String sql = """
        SELECT
            oi.product_id,
            SUM(oi.quantity) AS total_quantity
        FROM tbl_order_item oi
        INNER JOIN tbl_order o ON o.id = oi.order_id
        WHERE o.event_id = :eventId
          AND oi.product_id = 'ticket'
        GROUP BY oi.product_id
        """;

        var params = new MapSqlParameterSource()
                .addValue("eventId", eventId);

        return namedJdbcTemplate.query(sql, params, rs -> {
            Map<Long, Integer> result = new HashMap<>();
            while (rs.next()) {
                result.put(
                        rs.getLong("product_id"),
                        rs.getInt("total_quantity")
                );
            }
            return result;
        });
    }

    public long insert(OrderItemEntity item) {

        String sql = """
        INSERT INTO tbl_order_item (
            order_id,
            unit_price,
            quantity,
            subtotal,
            product_type,
            product_id
        )
        VALUES (
            :orderId,
            :unitPrice,
            :quantity,
            :subtotal,
            :productType,
            :productId
        )
        """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        namedJdbcTemplate.update(
                sql,
                new BeanPropertySqlParameterSource(item),
                keyHolder,
                new String[]{"id"} // 👈 nombre exacto de la PK
        );

        return keyHolder.getKey().longValue();
    }

}
