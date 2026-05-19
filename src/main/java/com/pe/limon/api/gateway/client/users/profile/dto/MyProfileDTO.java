package com.pe.limon.api.gateway.client.users.profile.dto;

public record MyProfileDTO (
    String username,
    String profileImgUrl,
    String name,
    String email,
    String phoneNumber
){}
