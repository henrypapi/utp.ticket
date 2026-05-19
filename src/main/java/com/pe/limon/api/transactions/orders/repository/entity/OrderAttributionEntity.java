package com.pe.limon.api.transactions.orders.repository.entity;

import com.pe.limon.api.transactions.promoters.bussiness.entity.PromoterEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class OrderAttributionEntity {
    private Long orderId;
    private Long promoterId;
    private PromoterEntity promoter;
    private Long registeredTimestamp;
    private LocalDateTime registeredDatetime;
}
