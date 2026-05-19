package com.pe.limon.api.transactions.payment.dto;

import jakarta.validation.constraints.NotNull;

public record CouponInDTO (
        @NotNull
        String code,
        @NotNull
        Float amount
){

}
