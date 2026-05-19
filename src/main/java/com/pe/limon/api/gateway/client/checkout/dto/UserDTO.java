package com.pe.limon.api.gateway.client.checkout.dto;

import jakarta.validation.constraints.NotNull;

public record UserDTO(

        @NotNull(message = "user.id es obligatorio")
        Long id
) {}