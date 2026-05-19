package com.pe.limon.api.transactions.payment.processors.impl.mercadopago;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.payment.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.pe.limon.api.core.utils.conversor.JsonUtils;
import com.pe.limon.api.core.utils.exception.BusinessException;
import com.pe.limon.api.transactions.authz.repository.PersonalInfoRepository;
import com.pe.limon.api.transactions.payment.dto.PaymentResult;
import com.pe.limon.api.transactions.payment.dto.generic.GenericPaymentDTO;

import com.pe.limon.api.transactions.wallet.business.TxStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class UtilsPayment {
    @Value("${payment.mercado-pago.access-token}")
    private String mercadoPagoAccessToken;

    private final PersonalInfoRepository personalInfoRepository;

    public Payment createPayment(GenericPaymentDTO payment) {
        log.info("[execute] Starting card payment {}",payment.toString());
        try {
            log.info("[MP] accessToken present={}, len={}, prefix={}",
                    mercadoPagoAccessToken != null,
                    mercadoPagoAccessToken == null ? 0 : mercadoPagoAccessToken.trim().length(),
                    mercadoPagoAccessToken == null || mercadoPagoAccessToken.trim().length() < 6
                            ? "null"
                            : mercadoPagoAccessToken.trim().substring(0, 6)
            );
            MercadoPagoConfig.setAccessToken(mercadoPagoAccessToken);
            PaymentClient paymentClient = new PaymentClient();
            var paymentCreateRequest = createRequest(payment);
            log.info("createdPayment {}", JsonUtils.convertToJsonString(paymentCreateRequest));
            return paymentClient.create(paymentCreateRequest);
        } catch (MPApiException apiException) {
            log.error("[createdPayment] MPApiException exception: {}",apiException.getMessage());
            throw new BusinessException(apiException.getApiResponse().getContent());
        } catch (MPException exception) {
            log.error("[createdPayment] MPException exception: {}",exception.getMessage());
            throw new BusinessException(exception.getMessage());
        }
    }

    public PaymentResult<Payment> payment(GenericPaymentDTO paymentDTO) {
        Payment createdPayment = createPayment(paymentDTO);
        var success = true;
        var message = "";
        var statusTx = TxStatusEnum.COMPLETED.getCode();
        switch (createdPayment.getStatus().toLowerCase()) {
            case "approved":
                switch (createdPayment.getStatusDetail().toLowerCase()) {
                    case "accredited":
                        break;
                    case "partially_refunded":
                        message = "⚠️ Pago aprobado pero parcialmente reembolsado";
                        break;
                    default:
                        message = "✅ Pago aprobado con detalle: " + createdPayment.getStatusDetail();
                }
                break;

            case "authorized":
                statusTx = TxStatusEnum.PENDING.getCode();
                if ("pending_capture".equalsIgnoreCase(createdPayment.getStatusDetail())) message = "⏳ Pago autorizado y pendiente de captura";
                else message = "⏳ Pago autorizado con detalle: " + createdPayment.getStatusDetail();
                break;

            case "in_process":
                statusTx = TxStatusEnum.PENDING.getCode();
                message = "⏳ Pago en proceso, estado_detail: " + createdPayment.getStatusDetail();
                break;

            case "pending":
                statusTx = TxStatusEnum.PENDING.getCode();
                message = "⏳ Pago pendiente, estado_detail: " + createdPayment.getStatusDetail();
                break;

            case "rejected":
                success = false;
                message = "❌ Pago rechazado, detalle: " + createdPayment.getStatusDetail();
                statusTx = TxStatusEnum.REJECTED.getCode();
                break;
        }

        return new PaymentResult<>(
                success,
                message,
                statusTx,
                createdPayment.getId().toString(),
                createdPayment.getStatus(),
                createdPayment
        );
    }

    public Payment getPaymentById(Long paymentId) {
        MercadoPagoConfig.setAccessToken(mercadoPagoAccessToken);
        PaymentClient paymentClient = new PaymentClient();
        try {
            return paymentClient.get(paymentId);
        } catch (MPApiException apiException) {
            log.error("[getPaymentById] MPApiException exception: {}",apiException.getMessage());
            throw new BusinessException(apiException.getApiResponse().getContent());
        } catch (MPException exception) {
            log.error("[execute] MPException exception: {}",exception.getMessage());
            throw new BusinessException(exception.getMessage());
        }
    }

    private PaymentCreateRequest createRequest(GenericPaymentDTO payment){
        //Long txId = transactionRepository.insert(new TransactionEntity());
        var personalInfo = personalInfoRepository.findByUserId(payment.order().getUserId());

        List<PaymentItemRequest> items = new ArrayList<>();
        for (var item : payment.order().getItems()){
            PaymentItemRequest paymentItem = PaymentItemRequest.builder()
                    .id(String.valueOf(Math.random())) // Código único o SKU del producto/servicio
                    .title(item.getTicketType().getName()) // Nombre del ítem
                    .description(item.getTicketType().getDescription()) // Descripción del ítem
                    .quantity(item.getQuantity()) // Cantidad comprada
                    .unitPrice(item.getUnitPrice()) // Precio unitario (BigDecimal)
                    .categoryId("entertainment")
                    .build();
            items.add(paymentItem);
        }
        PaymentAdditionalInfoRequest additionalInfo = PaymentAdditionalInfoRequest.builder()
                .items(items)
                .build();

        return PaymentCreateRequest.builder()
            .externalReference(payment.order().getId().toString())
            .transactionAmount(payment.order().getNetAmount())
            .token(payment.token())
            .description("Venta de ticket para evento: "+payment.order().getEventId())
            .installments(1)
            .payer(
                PaymentPayerRequest.builder()
                    .firstName(personalInfo.getFirstName())
                    .lastName(personalInfo.getLastName())
                    .email(personalInfo.getEmail())
                    .identification(
                        IdentificationRequest.builder()
                            .type(personalInfo.getDocumentType())
                            .number(personalInfo.getDocumentNumber())
                            .build()
                    )
                    .build()
            )
            .additionalInfo(additionalInfo)
            .build();
    }


    public static boolean verify(String xSignature, String xRequestId, String dataId, String secret) {
        if (isBlank(xSignature) || isBlank(xRequestId) || isBlank(dataId) || isBlank(secret)) return false;

        Map<String, String> parts = parseCommaKeyValue(xSignature); // ts=...,v1=...
        String ts = parts.get("ts");
        String v1 = parts.get("v1");
        if (isBlank(ts) || isBlank(v1)) return false;

        String manifest = "id:" + dataId + ";request-id:" + xRequestId + ";ts:" + ts + ";";

        String calculated = hmacSha256Hex(manifest, secret);

        // Comparación en tiempo constante
        return MessageDigest.isEqual(
                calculated.getBytes(StandardCharsets.UTF_8),
                v1.getBytes(StandardCharsets.UTF_8)
        );
    }

    private static Map<String, String> parseCommaKeyValue(String header) {
        Map<String, String> map = new HashMap<>();
        for (String piece : header.split(",")) {
            String[] kv = piece.split("=", 2);
            if (kv.length == 2) map.put(kv[0].trim(), kv[1].trim());
        }
        return map;
    }

    private static String hmacSha256Hex(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return toHexLower(raw);
        } catch (Exception e) {
            throw new RuntimeException("Error calculando HMAC SHA256", e);
        }
    }

    private static String toHexLower(byte[] bytes) {
        char[] hex = "0123456789abcdef".toCharArray();
        char[] out = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            out[i * 2] = hex[v >>> 4];
            out[i * 2 + 1] = hex[v & 0x0F];
        }
        return new String(out);
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

}
