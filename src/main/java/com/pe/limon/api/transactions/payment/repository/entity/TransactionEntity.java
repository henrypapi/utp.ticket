package com.pe.limon.api.transactions.payment.repository.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionEntity {
    private Long id;

    private String type;               // char
    private String operation;          // char
    private String methodId;           // char(10)
    private String status;             // char

    private BigDecimal amount;         // decimal(20,2)
    private String currency;           // char(3) -> PEN

    private String processorId;          // bigint
    private String processorStatus;    // varchar
    private String processorReference; // varchar

    private String metadata;            // json (se maneja como String)

    private LocalDateTime registeredDatetime;
    private Long registeredTimestamp;

    private LocalDateTime updatedDatetime;
    private Long updatedTimestamp;

    private String walletId;
}
