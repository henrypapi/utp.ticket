package com.pe.limon.api.gateway.admin.access.dto;

public record AccessPassFiltersDTO(
        String firstname,
        String lastname,
        String username,
        String email,
        String admissionStatus
) {
}