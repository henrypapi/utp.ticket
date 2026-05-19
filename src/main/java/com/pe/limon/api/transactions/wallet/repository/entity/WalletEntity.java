package com.pe.limon.api.transactions.wallet.repository.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class WalletEntity {
    private String id;
    private String status;
    private String currency;
    private BigDecimal balanceAvailable;
    private BigDecimal balanceHeld;
    private BigDecimal balanceTotal;
    private String lastTxnId;
    private LocalDateTime lastTxnDatetime;
    private Long lastTxnTimestamp;
    private LocalDateTime updatedDatetime;
    private Long updatedTimestamp;
    private LocalDateTime registeredDatetime;
    private Long registeredTimestamp;
    private String ownerUserId;
    private boolean allowWithdrawals;
}
