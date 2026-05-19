package com.pe.limon.api.transactions.payment.processors.impl.mercadopago;


import com.mercadopago.resources.payment.Payment;

import com.pe.limon.api.transactions.payment.dto.generic.GenericPaymentDTO;
import com.pe.limon.api.transactions.payment.dto.mercadopago.*;
import com.pe.limon.api.transactions.payment.dto.PaymentResult;
import com.pe.limon.api.transactions.payment.dto.generic.GetTokenDTO;
import com.pe.limon.api.transactions.payment.processors.BasePaymentProcessor;
import com.pe.limon.api.transactions.payment.processors.ISupportsFormToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class MercadoPagoYapeProcessor extends BasePaymentProcessor implements IMercadoPagoMethodProcessor, ISupportsFormToken {

    @Value("${payment.mercado-pago.public-endpoint.platform-payments}")
    private String paymentTokensEndpoint;
    private final RestTemplate rest;
    private final UtilsPayment utilsPayment;

    @Override
    public String getMethodName() {
        return "yape";
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public Optional<String> generateFormToken(GetTokenDTO request) {
        log.info("[generateFormToken] Starting ");
        YapeTokenDTO paymentDTO = (YapeTokenDTO) request;
        Map<String, Object> body = Map.of(
                "phoneNumber", paymentDTO.getPhone(),
                "otp", paymentDTO.getOtp()
        );

        HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String,Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> resp = rest.postForEntity(paymentTokensEndpoint, entity, Map.class);
        log.info("[generateFormToken] Status of request {}", resp.getStatusCode());

        if (resp.getStatusCode().is2xxSuccessful()) {
            String token = (String)(resp.getBody().get("id"));
            return Optional.of(token);
        }
        log.error("[generateFormToken] error to generate token {}", resp.getStatusCode());
        log.error("[generateFormToken] error to generate token body {}", resp.getBody());
        return Optional.empty();
    }


    @Override
    public PaymentResult<Payment> execute(GenericPaymentDTO request) {
        return utilsPayment.payment(request);
    }

}