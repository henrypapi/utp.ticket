package com.pe.limon.api.gateway.nosecure.webhooks;

import com.pe.limon.api.transactions.payment.processors.impl.mercadopago.UtilsPayment;
import com.pe.limon.api.transactions.wallet.business.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/public/webhooks/mercadopago")
@Slf4j
@RequiredArgsConstructor
public class MercadoPagoWebhookController {

    private final TransactionService transactionService;
    @Value("${payment.mercado-pago.webhook-key}")
    private String mpWebhookSecret;

    @PostMapping
    public ResponseEntity<String> onWebhook(
            @RequestHeader(value = "x-signature", required = false) String xSignature,
            @RequestHeader(value = "x-request-id", required = false) String xRequestId,
            @RequestBody Map<String, Object> payload
    ) {
        log.info("[onWebhook] payload: {}", payload);
        Object dataObj = payload.get("data");
        if (!(dataObj instanceof Map)) return ResponseEntity.ok("missing data");

        Map<String, Object> data = (Map<String, Object>) dataObj;
        Object idObj = data.get("id");
        if (idObj == null) return ResponseEntity.ok("missing id");
        String paymentId = String.valueOf(idObj);
        boolean valid = UtilsPayment.verify(
            xSignature,
            xRequestId,
            paymentId,
            mpWebhookSecret
        );

        if (!valid) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid signature");
        transactionService.processPayment(paymentId, "mercadopago");
        return ResponseEntity.ok("ok");
    }

    public record WebhookEvent(String action, String type, Data data) {
        public record Data(Long id) {}
    }
}
