package com.pe.limon.api.gateway.admin.wallet.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionDto {
    private Long id;
    private TransactionType type;
    private TransactionOperation operation;
    private String methodId;
    private BigDecimal amount;
    private Long timestamp;


}