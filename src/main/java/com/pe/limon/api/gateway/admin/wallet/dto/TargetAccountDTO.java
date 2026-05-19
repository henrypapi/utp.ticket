package com.pe.limon.api.gateway.admin.wallet.dto;

import java.math.BigDecimal;

public record TargetAccountDTO (
        String id,
        String type,
        String entity,
        String holderName,
        BigDecimal amount
){
}
