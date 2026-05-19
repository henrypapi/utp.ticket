package com.pe.limon.api.transactions.orders.business.checkout;

import com.pe.limon.api.transactions.orders.business.checkout.models.CheckoutContext;
import com.pe.limon.api.transactions.orders.business.checkout.models.PaymentMode;
import com.pe.limon.api.transactions.orders.business.checkout.models.CheckoutResponse;

public interface IProcessCheckout {

    /**
     * Modo de pago que maneja esta estrategia.
     */
    PaymentMode mode();

    /**
     * Ejecuta el checkout para el modo de pago correspondiente.
     * Puedes adaptar CheckoutContext/resultado a tu dominio.
     */
    CheckoutResponse process(CheckoutContext context);
}