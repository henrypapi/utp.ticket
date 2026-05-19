package com.pe.limon.api.transactions.payment.repository;


import com.pe.limon.api.transactions.payment.repository.entity.TransactionEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class PaymentRepository {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public Long insert(TransactionEntity transaction) {
        String sql = """
            
                INSERT INTO tbl_transaction (
                type,
                operation,
                status,
                method_id,
                amount,
                currency,
                processor_id,
                processor_status,
                processor_reference,
                registered_datetime,
                updated_datetime,
                registered_timestamp,
                updated_timestamp,
                wallet_id
            )
            VALUES (
                :type,
                :operation,
                :status,
                :methodId,
                :amount,
                :currency,
                :processorId,
                :processorStatus,
                :processorReference,
                :registeredDatetime,
                :updatedDatetime,
                :registeredTimestamp,
                :updatedTimestamp,
                :walletId
            )
            """;


        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedJdbcTemplate.update(sql, new BeanPropertySqlParameterSource(transaction), keyHolder, new String[]{"id"});

        Number key = keyHolder.getKey();
        Long generatedId = (key != null) ? key.longValue() : null;
        transaction.setId(generatedId);
        return generatedId;
    }
}
