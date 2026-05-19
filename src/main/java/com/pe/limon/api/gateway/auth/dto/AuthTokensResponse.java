package com.pe.limon.api.gateway.auth.dto;

import lombok.Builder;

@Builder
public record AuthTokensResponse(
        String tokenType,        // "Bearer"
        String accessToken,
        long accessExp,
        String refreshToken,
        long refreshExp,
        UserInfo user,
        String deviceId,
        String sessionId,
        boolean remember
) {
    @Builder
    public record UserInfo(
            String id,
            String email,
            String username,
            String profileImg,
            boolean emailVerified,
            boolean phoneVerified,
            boolean profileCompleted,
            boolean registrationCompleted) {}
}