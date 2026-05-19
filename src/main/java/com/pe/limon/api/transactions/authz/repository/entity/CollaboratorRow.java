package com.pe.limon.api.transactions.authz.repository.entity;

public record CollaboratorRow(
        long id,
        String userId,
        String ownerUserId,
        String username,
        String scopeType,      // "GLOBAL" o "EVENT"
        long eventCount,       // si EVENT
        String rolesSummary,   // "ADMIN, CHECKIN" (opcional)
        long registeredTimestamp
) {}