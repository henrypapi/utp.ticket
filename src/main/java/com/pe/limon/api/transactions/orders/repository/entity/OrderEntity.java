package com.pe.limon.api.transactions.orders.repository.entity;

import com.pe.limon.api.transactions.authz.repository.entity.UserEntity;
import com.pe.limon.api.transactions.events.repository.event.EventEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
public class OrderEntity {
    private Long id;
    private String userId;
    private UserEntity user;
    private Long eventId;
    private EventEntity event;
    private String status;
    private Integer totalQuantity;
    private BigDecimal totalAmount;
    private Long registeredTimestamp;
    private Long updatedTimestamp;
    private LocalDateTime registeredDatetime;
    private LocalDateTime updatedDatetime;
    private String paymentReference;
    private double commissionPercent;
    private BigDecimal commissionAmount;
    private BigDecimal netAmount;
    private List<OrderItemEntity> items;
    private OrderAttributionEntity orderAttribution;
    private String updatedBy;
    private String registeredBy;
}
