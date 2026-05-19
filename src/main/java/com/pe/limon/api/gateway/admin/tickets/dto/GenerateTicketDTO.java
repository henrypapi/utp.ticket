package com.pe.limon.api.gateway.admin.tickets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GenerateTicketDTO (
        @NotNull
        Long ticketTypeId,
        @NotBlank
        String user,
        @NotBlank
        Integer quantity
){
}
