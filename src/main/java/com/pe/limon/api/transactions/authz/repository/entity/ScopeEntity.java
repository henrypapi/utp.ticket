package com.pe.limon.api.transactions.authz.repository.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScopeEntity {
    private Long id;
    private Long collaboratorId;
    private String scopeType;
    private Long eventId;
    private LocalDateTime registeredDatetime;
    private Long registeredTimestamp;
}
