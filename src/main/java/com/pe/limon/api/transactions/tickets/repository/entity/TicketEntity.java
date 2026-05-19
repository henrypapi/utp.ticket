package com.pe.limon.api.transactions.tickets.repository.entity;

import com.pe.limon.api.transactions.events.repository.event.EventEntity;
import com.pe.limon.api.transactions.orders.repository.entity.OrderItemEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class TicketEntity {
    private Long id;
    private String labelName;
    private Long ticketTypeId;
    private TicketType ticketType;
    private Long registeredTimestamp;
    private LocalDateTime registeredDatetime;
    private EventEntity event;
    private Long eventId;
    private String registeredBy;
    private Long orderItemId;
    private OrderItemEntity orderItem;
    private List<AccessPassEntity> accessPassEntities;
}
