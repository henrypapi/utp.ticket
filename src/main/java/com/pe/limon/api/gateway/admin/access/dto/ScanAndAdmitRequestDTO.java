package com.pe.limon.api.gateway.admin.access.dto;

public record ScanAndAdmitRequestDTO(
    String code,
    String sig
) {}