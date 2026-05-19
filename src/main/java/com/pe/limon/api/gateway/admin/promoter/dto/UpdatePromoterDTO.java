package com.pe.limon.api.gateway.admin.promoter.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdatePromoterDTO(
        @NotNull @Positive @Min(1) Integer maxUses,
        @NotNull Boolean isActive
) {}