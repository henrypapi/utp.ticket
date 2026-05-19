package com.pe.limon.api.gateway.client.users.profile.dto;

public record UserInfoDTO (
        boolean exists,
        String userId,
        String displayName
){
}