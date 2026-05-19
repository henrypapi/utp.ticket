package com.pe.limon.api.gateway.admin.sales.dto;

public record SaleReponseDTO(Long id, String userId, Double amount,String status, String paymentReference, Long registerTimestamp) {
}