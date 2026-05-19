package com.pe.limon.api.transactions.orders.repository;

import com.pe.limon.api.transactions.orders.repository.entity.SalesOverviewEntity;
import com.pe.limon.api.transactions.orders.repository.entity.TicketTypeSalesEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SalesRepository {
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public SalesOverviewEntity getSalesOverviewByEventId(Long eventId, String orderStatus) {
        String sql = """
                SELECT
                  COALESCE(o.revenue, 0)      AS revenue,
                  COALESCE(tt.ticketsSold, 0) AS ticketsSold,
                  COALESCE(tt.totalTickets, 0) AS totalTickets,
                  CASE
                    WHEN COALESCE(tt.totalTickets, 0) > 0
                      THEN (COALESCE(tt.ticketsSold, 0) / COALESCE(tt.totalTickets, 0)) * 100
                    ELSE 0
                  END AS occupancyPercentage
                FROM
                (
                  SELECT SUM(net_amount) AS revenue
                  FROM tbl_order
                  WHERE event_id = :eventId
                    AND status = :orderStatus
                ) o
                CROSS JOIN
                (
                  SELECT
                    SUM(sold)  AS ticketsSold,
                    SUM(stock) AS totalTickets
                  FROM tbl_ticket_type
                  WHERE event_id = :eventId
                ) tt;
            """;

        var params = new MapSqlParameterSource().addValue("orderStatus", orderStatus).addValue("eventId", eventId);

        return namedJdbcTemplate.queryForObject(sql, params, (rs, rowNum) -> {
            SalesOverviewEntity entity = new SalesOverviewEntity();
            entity.setRevenue(rs.getBigDecimal("revenue"));
            entity.setTicketsSold(rs.getInt("ticketsSold"));
            entity.setTotalTickets(rs.getInt("totalTickets"));
            entity.setOccupancyPercentage(rs.getDouble("occupancyPercentage"));
            return entity;
        });
    }

    public List<TicketTypeSalesEntity> getTicketTypeSalesByEventId(Long eventId) {
        String sql = """
            SELECT
                 tt.id,
                 tt.name,
                 tt.sold,
                 tt.stock
             FROM tbl_ticket_type tt
                      LEFT JOIN tbl_ticket t
                                ON t.ticket_type_id = tt.id
             WHERE tt.event_id = :eventId
             GROUP BY tt.id, tt.name, tt.stock
             ORDER BY tt.id;
            """;

        var params = new MapSqlParameterSource().addValue("eventId", eventId);

        return namedJdbcTemplate.query(sql, params, (rs, rowNum) -> {
            TicketTypeSalesEntity entity = new TicketTypeSalesEntity();
            entity.setId(rs.getLong("id"));
            entity.setName(rs.getString("name"));
            entity.setSold(rs.getInt("sold"));
            entity.setStock(rs.getInt("stock"));
            return entity;
        });
    }
}
