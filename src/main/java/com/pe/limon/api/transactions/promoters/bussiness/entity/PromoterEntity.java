package com.pe.limon.api.transactions.promoters.bussiness.entity;


import com.pe.limon.api.transactions.authz.repository.entity.UserEntity;
import com.pe.limon.api.transactions.events.repository.event.EventEntity;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PromoterEntity {
    private Long id;
    private Long eventId;
    private EventEntity event;
    private String code;
    private String promoterUserId;
    private UserEntity promoterUser;
    private Integer maxUses;
    private Integer totalSalesCount;
    private BigDecimal totalSalesAmount;
    private String totalSalesDetails;
    private Long totalUses;
    private boolean active;
    private LocalDateTime registeredDatetime;
    private Long registeredTimestamp;
    private String registeredBy;
}