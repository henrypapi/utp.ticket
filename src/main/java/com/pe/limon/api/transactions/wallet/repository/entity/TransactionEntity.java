package com.pe.limon.api.transactions.wallet.repository.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionEntity {
    private Long id;
    private String type;
    private String operation;
    private String methodId;
    private String status;
    private BigDecimal amount;
    private String currency;
    private String processorId;
    private String processorStatus;
    private String processorReference;
    private String metadata;
    private LocalDateTime registeredDatetime;
    private Long registeredTimestamp;
    private LocalDateTime updatedDatetime;
    private Long updatedTimestamp;
    private String walletId;


}