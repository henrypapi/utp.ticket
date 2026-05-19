package com.pe.limon.api.transactions.payment.dto;

public record DiscountDTO (
        Float initialAmount,
        Float finalAmount,
        Float discountedAmount

){
}
