package com.pe.limon.api.gateway.admin.sales.dto;

public record TicketTypeSalesDTO(
    long id,
    String name,
    int sold,
    int stock
) {
}