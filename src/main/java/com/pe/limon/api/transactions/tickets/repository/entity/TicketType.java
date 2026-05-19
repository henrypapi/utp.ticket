package com.pe.limon.api.transactions.tickets.repository.entity;

import com.pe.limon.api.transactions.events.repository.event.EventEntity;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ToString
public class TicketType {
    private Long id;
    private Long eventId;
    private EventEntity event;
    private String name;
    private String description;
    private String status;
    private BigDecimal price;
    private int stock;
    private int reserved;
    private int sold;
    private Integer purchaseQuantityLimit;
    private Integer seatsPerGroup;
    private boolean active;
    private boolean enableVoucher;
    private LocalDateTime enableStartDatetime;
    private LocalDateTime enableEndDatetime;
    private Long enableStartTimestamp;
    private Long enableEndTimestamp;
    private Long registeredTimestamp;
    private LocalDateTime registeredDatetime;

    private LocalDateTime validFromDatetime;
    private LocalDateTime validUntilDatetime;
    private Long validFromTimestamp;
    private Long validUntilTimestamp;
    private String registeredBy;
}
