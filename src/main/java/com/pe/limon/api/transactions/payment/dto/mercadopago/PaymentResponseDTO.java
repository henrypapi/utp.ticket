package com.pe.limon.api.transactions.payment.dto.mercadopago;

public record PaymentResponseDTO(
        Long id,
        String status,
        String detail
) {
}
