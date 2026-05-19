package com.pe.limon.api.gateway.admin.wallet.dto;

import java.math.BigDecimal;

public record WalletRequest(
        String id,
        String type,
        String entity,
        String holderName,
        BigDecimal amount
){
}
