package com.pe.limon.api.transactions.payment.processors.impl.mercadopago;

import com.pe.limon.api.transactions.payment.processors.IPaymentProcessor;

public interface IMercadoPagoMethodProcessor extends IPaymentProcessor {
    String getMethodName(); // "yape", "tarjeta", etc.
}
