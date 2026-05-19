package com.pe.limon.api.transactions.wallet.repository;

import com.pe.limon.api.transactions.wallet.repository.entity.WalletRequestEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class WalletRequestRepository {
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public int inert(WalletRequestEntity entity){
        String sql = """
                INSERT INTO tbl_wallet_requests (
                    request_id,
                    wallet_id,
                    request_type,
                    amount,
                    currency,
                    requested_by_user_id,
                    description,
                    attachments_json,
                    status,
                    registered_datetime,
                    registered_timestamp
                ) VALUES (
                    :requestId,
                    :walletId,
                    :requestType,
                    :amount,
                    :currency,
                    :requestByUserId,
                    :description,
                    :attachmentsJson,
                    :status,
                    :registeredDatetime,
                    :registeredTimestamp
                );
                """;

        return namedJdbcTemplate.update(sql, new BeanPropertySqlParameterSource(entity) );
    }
}
