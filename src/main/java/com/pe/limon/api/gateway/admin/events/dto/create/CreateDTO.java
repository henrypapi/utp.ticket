package com.pe.limon.api.gateway.admin.events.dto.create;


import jakarta.validation.constraints.NotNull;

public record CreateDTO(
        @NotNull
        BasicInfoDTO basicInfo,
        @NotNull
        LocationInfoDTO location,
        @NotNull
        TermsInfoDTO terms
) {
}
