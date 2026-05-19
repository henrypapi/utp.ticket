package com.pe.limon.api.gateway.admin.tickets.dto.types;

import jakarta.validation.constraints.NotNull;


public record CreateDTO(
        @NotNull
        Long eventId,
        @NotNull
        String name,
        @NotNull
        String description,
        Float realPrice,
        @NotNull
        Integer stock,
        @NotNull
        boolean free,
        @NotNull
        boolean enableVoucher,
        @NotNull
        boolean active,
        Long validFromTimestamp,
        Integer qrDurationHours,
        @NotNull
        Long enableStartTimestamp,
        @NotNull
        Long enableEndTimestamp,
        @NotNull
        Integer purchaseQuantityLimit,
        @NotNull
        Integer seatsPerGroup
) {
}
