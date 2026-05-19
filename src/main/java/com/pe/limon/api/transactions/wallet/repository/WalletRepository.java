package com.pe.limon.api.transactions.wallet.repository;

import com.pe.limon.api.transactions.wallet.repository.entity.WalletEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class WalletRepository {
    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    public WalletEntity finByEventId(Long eventId) {
        String sql = """
        SELECT o.*
        FROM tbl_wallet o
        INNER JOIN tbl_event e ON o.owner_user_id = e.owner_user_id
        WHERE e.id = :eventId
        """;
        return namedJdbcTemplate.queryForObject(sql, params(eventId),
                new BeanPropertyRowMapper<>(WalletEntity.class));
    }

    public boolean existsByOwnerUserId(String userId) {
        String sql = """
        SELECT COUNT(*)
        FROM tbl_wallet
        WHERE owner_user_id = :userId
        """;
        var count = namedJdbcTemplate.queryForObject(sql, new MapSqlParameterSource().addValue("userId", userId),
                Integer.class) ;

        return count != null && count > 0;
    }

    public void insert(WalletEntity walletEntity){
        String sql = """
                INSERT INTO tbl_wallet (
                      id,
                      owner_user_id,
                      currency,
                      balance_available,
                      balance_held,
                      status,
                      allow_withdrawals,
                      registered_datetime,
                      registered_timestamp
                  )
                  VALUES (
                      :id,
                      :ownerUserId,
                      :currency,
                      :balanceAvailable,
                      :balanceHeld,
                      :status,
                      :allowWithdrawals,
                      :registeredDatetime,
                      :registeredTimestamp
                  );
                """;

        namedJdbcTemplate.update(sql,new BeanPropertySqlParameterSource(walletEntity));
    }

    public WalletEntity finByEventIdForUpdate(Long eventId) {

        String sql = """
            SELECT o.*
            FROM tbl_wallet o
            INNER JOIN tbl_event e
                ON o.owner_user_id = e.owner_user_id
            WHERE e.id = :eventId
            FOR UPDATE
        """;
        return namedJdbcTemplate.queryForObject(sql, params(eventId),
                new BeanPropertyRowMapper<>(WalletEntity.class));
    }

    private MapSqlParameterSource params(Long eventId){
        return new MapSqlParameterSource().addValue("eventId", eventId);
    }


    public WalletEntity finByOwnerUserId(String ownerUserId) {
        String sql = """
            SELECT
                *
            FROM tbl_wallet w
            WHERE w.owner_user_id = :ownerUserId
            """;

        var params = new MapSqlParameterSource()
                .addValue("ownerUserId", ownerUserId);

        return namedJdbcTemplate.queryForObject(sql, params, new BeanPropertyRowMapper<>(WalletEntity.class));
    }

    public long updateBalance(WalletEntity transaction) {
        log.info("[updateBalance] Refresh balance : {}", transaction);
        String sql = """
            UPDATE tbl_wallet SET
                balance_available = :balanceAvailable,
                balance_held = :balanceHeld,
                last_txn_id = :lastTxnId,
                last_txn_datetime = :lastTxnDatetime,
                last_txn_timestamp = :lastTxnTimestamp,
                updated_datetime = :updatedDatetime,
                updated_timestamp= :updatedTimestamp
            WHERE id=:id
        """;
        return namedJdbcTemplate.update(sql, new BeanPropertySqlParameterSource(transaction));
    }
}
