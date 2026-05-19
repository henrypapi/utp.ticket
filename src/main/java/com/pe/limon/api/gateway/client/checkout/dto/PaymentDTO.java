package com.pe.limon.api.gateway.client.checkout.dto;

import jakarta.validation.constraints.NotNull;

public record PaymentDTO(
        @NotNull
        String method,
        @NotNull
        String processorId,
        @NotNull
        String token
){

}
