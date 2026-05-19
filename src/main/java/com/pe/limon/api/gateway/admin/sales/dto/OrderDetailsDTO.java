package com.pe.limon.api.gateway.admin.sales.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailsDTO(
    Long id,
    Long createdTimestamp,
    Integer totalQuantity,
    BigDecimal totalAmount,
    BigDecimal totalNetAmount,
    BigDecimal commissionAmount,
    String paymentReference,
    String status,
    List<OrderItemDTO> items,
    BuyerDTO buyer
) {
}