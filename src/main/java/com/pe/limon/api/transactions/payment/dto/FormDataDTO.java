package com.pe.limon.api.transactions.payment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record FormDataDTO(
        @NotNull(message = "customer is required")
        @Valid
        CustomerDTO customer,
        @NotNull
        Integer planId,
        String coupon
) {
}
