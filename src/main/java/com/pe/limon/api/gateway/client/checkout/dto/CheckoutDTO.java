package com.pe.limon.api.gateway.client.checkout.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CheckoutDTO (
        @NotNull
        PaymentDTO payment,
        @NotNull
        Long eventId,
        @NotNull
        List<ItemsDTO> items,
        AttributionDTO attribution
){

}
