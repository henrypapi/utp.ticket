package com.pe.limon.api.gateway.admin.events.dto.create;

import jakarta.validation.constraints.NotNull;

public record LocationInfoDTO (
    String address,
    double latitude,
    double longitude,
    String metadata
){
}
