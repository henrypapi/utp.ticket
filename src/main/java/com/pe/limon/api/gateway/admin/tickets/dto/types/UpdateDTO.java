package com.pe.limon.api.gateway.admin.tickets.dto.types;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record UpdateDTO(
        @NotNull
        String name,
        @NotNull
        String description,
        Float realPrice,
        @NotNull
        Integer stock,
        @NotNull
        boolean free,
        boolean enableVoucher,
        @NotNull
        boolean active,
        @NotNull
        Long validFromTimestamp,
        Integer qrDurationHours,
        Long enableStartTimestamp,
        Long enableEndTimestamp,
        @NotNull
        Integer purchaseQuantityLimit,
        @NotNull
        Integer seatsPerGroup
) {

}
