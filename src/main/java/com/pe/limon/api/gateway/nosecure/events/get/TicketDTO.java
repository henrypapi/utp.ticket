package com.pe.limon.api.gateway.nosecure.events.get;

public record TicketDTO(
        Long id,
        String name,
        String description,
        java.math.BigDecimal price,
        Integer purchaseQuantityLimit,
        Integer seatsPerGroup
) {
}
