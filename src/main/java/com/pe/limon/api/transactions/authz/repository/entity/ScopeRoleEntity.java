package com.pe.limon.api.transactions.authz.repository.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScopeRoleEntity {
    private String username;
    private Long scopeId;
    private String scopeType;
    private Long roleId;
    private Long eventIdKey;
    private LocalDateTime registeredDatetime;
    private Long registeredTimestamp;
}
