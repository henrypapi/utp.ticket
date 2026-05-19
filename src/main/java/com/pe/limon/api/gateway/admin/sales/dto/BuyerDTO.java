package com.pe.limon.api.gateway.admin.sales.dto;

public record BuyerDTO(
    String firstname,
    String lastname,
    String email,
    String phoneNumber
) {
}