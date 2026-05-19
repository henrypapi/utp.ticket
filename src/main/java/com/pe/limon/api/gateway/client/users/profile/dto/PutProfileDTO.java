package com.pe.limon.api.gateway.client.users.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record PutProfileDTO (
        @NotNull
        @NotBlank
        String username,
        MultipartFile profileImage,
        @NotNull
        @NotBlank
        String name,
        @NotNull
        @NotBlank
        String phoneNumber
){
}
