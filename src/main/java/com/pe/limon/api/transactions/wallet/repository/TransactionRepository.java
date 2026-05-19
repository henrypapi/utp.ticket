package com.pe.limon.api.transactions.wallet.repository;

import com.pe.limon.api.gateway.admin.events.dto.PageResult;
import com.pe.limon.api.transactions.wallet.repository.entity.TransactionEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
@Slf4j
public class TransactionRepository {

    private final NamedParameterJdbcTemplate namedJdbc;

    @Value("${application.transaction.days-show}")
    private int daysShow;

    public PageResult<TransactionEntity> findByDays(String userId, int page, int size) {

        StringBuilder sql = new StringBuilder("""
            SELECT
             t.id,
             t.type,
             t.operation,
             t.method_id,
             t.status,
             t.amount,
             t.currency,
             t.processor_id,
             t.processor_status,
             t.processor_reference,
             t.metadata,
             t.registered_timestamp,
             t.registered_datetime,
             t.updated_datetime,
             t.updated_timestamp
         FROM tbl_transaction t
         INNER JOIN tbl_wallet w ON t.wallet_id=  w.id
         WHERE t.registered_timestamp >= (UNIX_TIMESTAMP() - (:daysShow * 24 * 60 * 60))
         AND w.owner_user_id = :userId
        """);

        StringBuilder countSql = new StringBuilder("""
            SELECT COUNT(*)
            FROM tbl_transaction t
             INNER JOIN tbl_wallet w ON t.wallet_id=  w.id
             WHERE t.registered_timestamp >= (UNIX_TIMESTAMP() - (:daysShow * 24 * 60 * 60))
             AND w.owner_user_id = :userId
        """);

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("daysShow", daysShow);
        params.addValue("userId", userId);

        sql.append(" ORDER BY t.registered_timestamp DESC LIMIT :limit OFFSET :offset");

        int offset = (page - 1) * size;
        params.addValue("limit", size);
        params.addValue("offset", offset);

        log.info("[findByFilters] Main query: {}", sql);
        log.info("[findByFilters] Count query: {}", countSql);
        log.info("[findByFilters] Params: {}", params);

        List<TransactionEntity> list = namedJdbc.query(sql.toString(), params,
            (rs, rowNum) -> {
                TransactionEntity dto = new TransactionEntity();
                dto.setId(rs.getLong("id"));
                dto.setType(rs.getString("type"));
                dto.setOperation(rs.getString("operation"));
                dto.setMethodId(rs.getString("method_id"));
                dto.setStatus(rs.getString("status"));
                dto.setAmount(rs.getBigDecimal("amount"));
                dto.setCurrency(rs.getString("currency"));
                dto.setProcessorId(rs.getString("processor_id"));
                dto.setProcessorStatus(rs.getString("processor_status"));
                dto.setProcessorReference(rs.getString("processor_reference"));
                dto.setMetadata(rs.getString("metadata"));
                Timestamp createdTs = rs.getTimestamp("registered_datetime");
                if (createdTs != null) {
                    dto.setRegisteredDatetime(createdTs.toLocalDateTime());
                }
                dto.setRegisteredTimestamp(rs.getLong("registered_timestamp"));
                Timestamp updatedTs = rs.getTimestamp("updated_datetime");
                if (updatedTs != null) {
                    dto.setUpdatedDatetime(updatedTs.toLocalDateTime());
                }
                dto.setUpdatedTimestamp(rs.getLong("updated_timestamp"));
                return dto;
            }
        );
       
        Long total = namedJdbc.queryForObject(countSql.toString(), params, Long.class);

        return new PageResult<>(list, page, size, total);
    }


    public long insert(TransactionEntity transaction) {

        String sql = """
            INSERT INTO tbl_transaction (
                type,
                operation,
                method_id,
                status,
                amount,
                currency,
                processor_id,
                metadata,
                registered_datetime,
                registered_timestamp,
                updated_datetime,
                updated_timestamp,
                wallet_id,
                processor_reference
            ) VALUES (
                :type,
                :operation,
                :methodId,
                :status,
                :amount,
                :currency,
                :processorId,
                :metadata,
                :registeredDatetime,
                :registeredTimestamp,
                :updatedDatetime,
                :updatedTimestamp,
                :walletId,
                :processorReference
            )
        """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        namedJdbc.update(sql, new BeanPropertySqlParameterSource(transaction), keyHolder);

        return Objects.requireNonNull(keyHolder.getKey()).longValue(); // transaction.id generado
    }


    public long updateStatus(TransactionEntity transaction) {
        String sql = """
            UPDATE tbl_transaction SET
                metadata = :metadata,
                status = :status,
                processor_status = :processorStatus,
                updated_datetime = :updatedDatetime,
                updated_timestamp= :updatedTimestamp
            WHERE processor_reference=:processorReference and processor_id=:processorId and type=:type and operation=:operation
        """;
        return namedJdbc.update(sql, new BeanPropertySqlParameterSource(transaction));
    }

    public TransactionEntity findByProcessorRefAndProcessorId(String processorRef, String processorId, String type, String operation) {
        try {
            String sql = """
            SELECT
                *
            FROM tbl_transaction
            WHERE processor_reference=:processorRef and processor_id=:processorId and type=:type and operation=:operation
            """;

            var params = new MapSqlParameterSource()
                    .addValue("processorId", processorId)
                    .addValue("type", type)
                    .addValue("operation", operation)
                    .addValue("processorRef", processorRef);

            return  namedJdbc.queryForObject(sql,params, TransactionEntity.class);
        }catch (EmptyResultDataAccessException e){
            return null;
        }
    }

    public TransactionEntity findById(Long id) {
        try {
            String sql = """
                SELECT
                    *
                FROM tbl_transaction
                WHERE id = :id
            """;

            var params = new MapSqlParameterSource()
                    .addValue("id", id);

            return  namedJdbc.queryForObject(sql,params, TransactionEntity.class);
        }catch (EmptyResultDataAccessException e){
            return null;
        }
    }

}