package com.pe.limon.api.gateway.admin.sales.dto;

import java.math.BigDecimal;

public record OrderItemDTO(
    Long id,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal subtotal,
    TicketDTO ticket
) {
}