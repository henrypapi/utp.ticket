package com.pe.limon.api.transactions.orders.repository;


import com.pe.limon.api.transactions.orders.repository.entity.OrderAttributionEntity;
import com.pe.limon.api.transactions.promoters.bussiness.entity.PromoterEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderAttributionRepository {
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public OrderAttributionEntity findById(Long orderId) {
        try {
            String sql = """
            SELECT
                ep.total_sales_count,
                ep.total_sales_amount,
                ep.total_sales_details,
                oa.order_id,
                oa.promoter_id
            FROM tbl_order_attributions oa
            INNER JOIN tbl_event_promoter ep
            ON oa.promoter_id = ep.id
            WHERE oa.order_id = :orderId
            """;

            var params = new MapSqlParameterSource().addValue("orderId", orderId);

            return namedJdbcTemplate.queryForObject(sql, params, (rs, rowNum) -> {
                OrderAttributionEntity entity = new OrderAttributionEntity();
                entity.setOrderId(rs.getLong("order_id"));
                PromoterEntity promoter = new PromoterEntity();
                promoter.setId(rs.getLong("promoter_id"));
                promoter.setTotalSalesDetails(rs.getString("total_sales_details"));
                promoter.setTotalSalesCount(rs.getInt("total_sales_count"));
                promoter.setTotalSalesAmount(rs.getBigDecimal("total_sales_amount"));
                entity.setPromoter(promoter);
                return entity;
            });
        }catch (DataAccessException e) {
            return null;
        }
    }


    public boolean insert(OrderAttributionEntity order) {
        String sql = """
            INSERT INTO tbl_order_attributions (
                order_id,
                promoter_id,
                registered_datetime,
                registered_timestamp
            )
            VALUES (
                :orderId,
                :promoterId,
                :registeredDatetime,
                :registeredTimestamp
            )
            """;

        return namedJdbcTemplate.update(sql, new BeanPropertySqlParameterSource(order))>0;
    }


}
