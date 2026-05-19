package com.pe.limon.api.gateway.auth.dto;

import com.pe.limon.api.transactions.authz.business.permissions.EffectivePerm;

import java.util.Map;

public record AllPermsResponseDTO(
        String userId,
        long fetchedAtEpoch,
        Map<Long, EffectivePerm> permissions
) {}