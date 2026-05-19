package com.pe.limon.api.gateway.admin.collaborators.dto;

import jakarta.validation.constraints.NotBlank;
public record CreateCollabDTO (
        @NotBlank
        String user
){
}
