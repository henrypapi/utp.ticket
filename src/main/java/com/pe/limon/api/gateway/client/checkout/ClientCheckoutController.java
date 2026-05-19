package com.pe.limon.api.gateway.client.checkout;

import com.pe.limon.api.core.utils.exception.BusinessException;
import com.pe.limon.api.core.utils.file.FileUtil;
import com.pe.limon.api.gateway.client.checkout.dto.CheckoutDTO;
import com.pe.limon.api.gateway.client.checkout.dto.VoucherDTO;
import com.pe.limon.api.transactions.orders.business.checkout.CheckoutExecutor;
import com.pe.limon.api.transactions.payment.dto.mercadopago.YapeTokenDTO;
import com.pe.limon.api.transactions.payment.processors.IPaymentProcessor;
import com.pe.limon.api.transactions.payment.processors.ISupportsFormToken;
import com.pe.limon.api.transactions.payment.processors.ProcessorFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("client/checkout")
@RequiredArgsConstructor
@Slf4j
public class ClientCheckoutController {

    private final ProcessorFactory processorFactory;
    private final CheckoutExecutor checkoutExecutor;

    @PostMapping("/process")
    public ResponseEntity<?> chargeEntity(
            @RequestAttribute String userId,
            HttpServletRequest request,
            @Valid @RequestBody CheckoutDTO dto
    ) {
        log.info("Request {}", dto);
        return ResponseEntity.ok(checkoutExecutor.execute(userId, getClientIp(request),dto));
    }

    @PostMapping(value = "/voucher", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> chargeEntity(
            @RequestAttribute String userId,
            HttpServletRequest request,
            @Valid @ModelAttribute VoucherDTO dto
    ){
        log.info("Request {}", dto);

        if (dto.getVoucher() != null) {
            log.debug("Imagen enviada, realizando validacion");
            if (!FileUtil.isValidFile(dto.getVoucher(), FileUtil.ALLOWED_EXTENSIONS_IMAGE)) throw new BusinessException("Archivo de imagen inválido");
        }
        return ResponseEntity.ok(checkoutExecutor.execute(userId, getClientIp(request),dto));
    }

    public String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // Puede traer varias IPs separadas por coma
            return ip.split(",")[0].trim();
        }

        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getHeader("CF-Connecting-IP"); // si usas Cloudflare
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        return request.getRemoteAddr(); // fallback (IP del proxy si no hay headers)
    }

    @PostMapping("/tokens/{processor}/{method}")
    public ResponseEntity<Object> generateTokens(
            @PathVariable String processor,
            @PathVariable String method,
            @RequestBody YapeTokenDTO tokenDTO,
            @RequestAttribute String userId) throws BusinessException {
        IPaymentProcessor paymentProcessor =  processorFactory.getProcessor(processor);
        log.info("Generating tokens for payment processor {} - for user {}", paymentProcessor, userId);
        if (paymentProcessor.isAsync()) {
            log.info("Generating tokens for payment processor {} - for user {}", paymentProcessor.isAsync(), userId);
            try {
                tokenDTO.setMethod(method);
                ISupportsFormToken tokenCapable = (ISupportsFormToken) paymentProcessor;
                return ResponseEntity.ok().body(Map.of("token",tokenCapable.generateFormToken(tokenDTO)));
            } catch (Exception e) {
                log.error("Error generating form token for method '{}': {} - for user {}", method, e.getMessage() , userId);
                return ResponseEntity.internalServerError().body("");
            }
        } else {
            log.warn("Processor '{}' does not support async operations (form token not supported).", method);
            return ResponseEntity.internalServerError().body("");
        }
    }
}
