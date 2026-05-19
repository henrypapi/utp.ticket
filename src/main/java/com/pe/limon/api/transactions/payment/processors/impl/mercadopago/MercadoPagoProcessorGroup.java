package com.pe.limon.api.transactions.payment.processors.impl.mercadopago;

import com.pe.limon.api.transactions.payment.dto.generic.GenericPaymentDTO;

import com.pe.limon.api.transactions.payment.dto.PaymentResult;
import com.pe.limon.api.transactions.payment.dto.generic.GetTokenDTO;
import com.pe.limon.api.transactions.payment.processors.IPaymentProcessor;
import com.pe.limon.api.transactions.payment.processors.ISupportsFormToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component("mercadopago")
public class MercadoPagoProcessorGroup implements IPaymentProcessor, ISupportsFormToken {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoProcessorGroup.class);
    private final Map<String, IPaymentProcessor> methodProcessors;

    public MercadoPagoProcessorGroup(List<IPaymentProcessor> allProcessors) {
        this.methodProcessors = allProcessors.stream()
                .filter(p -> p instanceof IMercadoPagoMethodProcessor)
                .map(p -> (IMercadoPagoMethodProcessor) p)
                .collect(Collectors.toMap(IMercadoPagoMethodProcessor::getMethodName, Function.identity()));
    }

    @Override
    public PaymentResult execute(GenericPaymentDTO request) {
        String method = request.method();
        IPaymentProcessor processor = methodProcessors.get(method);
        log.info("[execute] methodProcessor: {}", request);
        log.info("[execute] methodProcessor: {}", methodProcessors);
        log.info("[execute] methodProcessor: {}", methodProcessors.get("card"));
        log.info("[execute] method: {}", method);
        log.info("[execute] processor: {}", processor);
        if (processor == null) {
            return PaymentResult.failure("Método no soportado: " + method);
        }
        return processor.execute(request);
    }

    @Override
    public Optional<String> generateFormToken(GetTokenDTO dto) {
        String method = dto.getMethod(); // asegúrate de tener este campo
        IPaymentProcessor processor = methodProcessors.get(method);
        log.info("methodProcessor: {}", methodProcessors);
        log.info("methodProcessor: {}", methodProcessors.get("yape"));
        log.info("processor: {}", processor);
        if (processor instanceof ISupportsFormToken tokenCapable) {
            return tokenCapable.generateFormToken(dto);
        }
        throw new UnsupportedOperationException("El método no soporta generación de form token");
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}