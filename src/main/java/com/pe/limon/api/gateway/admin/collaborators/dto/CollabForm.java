package com.pe.limon.api.gateway.admin.collaborators.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CollabForm (
        @NotNull
        String user,
        @NotBlank
        String scopeType,
        @Size(min = 1)
        @NotNull
        List<Long> eventIds,
        @NotNull
        @Size(min = 1)
        List<Long> roleIds
){
}
