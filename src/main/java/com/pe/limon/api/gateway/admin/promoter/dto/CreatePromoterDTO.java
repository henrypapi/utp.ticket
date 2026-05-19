package com.pe.limon.api.gateway.admin.promoter.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreatePromoterDTO (
        @NotNull @NotBlank @Size(min = 8, max = 15) String code,
        @NotNull @NotBlank @Size(min = 12, max = 12) String promoterUser,
        @NotNull @Positive @Min(1) Integer maxUses,
        @NotNull Boolean isActive
){}