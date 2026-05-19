package com.pe.limon.api.transactions.payment.processors;


import com.pe.limon.api.transactions.payment.dto.PaymentResult;
import com.pe.limon.api.transactions.payment.dto.generic.GenericPaymentDTO;

public interface IPaymentProcessor {
    PaymentResult execute(GenericPaymentDTO request);
    default boolean isAsync() {
        return false;
    }
}
