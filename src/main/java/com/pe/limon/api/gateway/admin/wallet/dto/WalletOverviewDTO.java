package com.pe.limon.api.gateway.admin.wallet.dto;

public record WalletOverviewDTO (
        Long balance,
        Long deposits,
        Long withdrawal
){
}
