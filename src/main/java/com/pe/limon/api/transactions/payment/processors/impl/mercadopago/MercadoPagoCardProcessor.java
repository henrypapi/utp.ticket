package com.pe.limon.api.transactions.payment.processors.impl.mercadopago;

import com.pe.limon.api.transactions.payment.dto.generic.GenericPaymentDTO;
import com.pe.limon.api.transactions.payment.dto.PaymentResult;
import com.pe.limon.api.transactions.payment.processors.BasePaymentProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class MercadoPagoCardProcessor extends BasePaymentProcessor implements IMercadoPagoMethodProcessor {

    private final UtilsPayment utilsPayment;

    @Override
    public String getMethodName() {
        return "card";
    }

    @Override
    public boolean isAsync() {
        return super.isAsync();
    }

    @Override
    public PaymentResult execute(GenericPaymentDTO dto) {
        log.info("[execute] Starting card payment {}",dto.toString());
        return utilsPayment.payment(dto);
    }
}
