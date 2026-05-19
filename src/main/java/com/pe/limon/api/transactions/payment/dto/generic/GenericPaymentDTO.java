package com.pe.limon.api.transactions.payment.dto.generic;


import com.pe.limon.api.transactions.orders.repository.entity.OrderEntity;

public record GenericPaymentDTO (
         String method,
         String processor,
         OrderEntity order,
         String ip,
         String token
){
}
