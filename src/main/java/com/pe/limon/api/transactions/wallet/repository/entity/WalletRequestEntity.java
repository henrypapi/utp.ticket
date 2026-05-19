package com.pe.limon.api.transactions.wallet.repository.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WalletRequestEntity {
    private String walletId;
    private String requestId;
    private String requestType;
    private BigDecimal amount;
    private String currency;
    private String requestByUserId;
    private String description;
    private String attachmentsJson;
    private String status;
    private String approvedByUserId;
    private String rejectedByUserId;
    private LocalDateTime registeredDatetime;
    private Long registeredTimestamp;
    private LocalDateTime approvedDatetime;
    private Long approvedTimestamp;
    private LocalDateTime rejectedDatetime;
    private Long rejectedTimestamp;
}
