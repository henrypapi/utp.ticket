package com.pe.limon.api.transactions.payment.dto;

public record TokenDTO(
        String message,
        String token,
        String publicKey
) {
}
