package com.pe.limon.api.transactions.orders.business.checkout.models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class CheckoutProcessor extends CheckoutContext{
    private String method;
    private String processor;
    private String ip;
    private String token;
}
