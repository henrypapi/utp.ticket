package com.pe.limon.api.transactions.authz.repository.entity;

import lombok.Data;

import java.time.Instant;

@Data
public class UserSessionEntity {
    private String id;
    private String userId;
    private String deviceId;
    private String refreshHash;
    private String userAgent;
    private String ipHash;
    private Instant expiresAt;
    private boolean revoked;
    private Boolean remember;
}
