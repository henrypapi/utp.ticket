package com.pe.limon.api.gateway.admin.access.dto;

public record AttendeeDTO(
    String userId,
    String firstName,
    String lastName,
    String email,
    String phoneNumber,
    String docType,
    String docNumber
) {}