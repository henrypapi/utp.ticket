package com.pe.limon.api.transactions.orders.repository.entity;

import com.pe.limon.api.transactions.tickets.repository.entity.TicketType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemEntity {
    private Long id;
    private Long orderId;
    private OrderEntity order;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private Long productId;
    private String productType;
    private TicketType ticketType;
}
