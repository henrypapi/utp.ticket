package com.pe.limon.api.transactions.payment.processors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class ProcessorFactory {

    private final Map<String, IPaymentProcessor> processorMap;

    public ProcessorFactory(Map<String, IPaymentProcessor> processorMap) {
        this.processorMap = processorMap;
    }

    public IPaymentProcessor getProcessor(String processor) {
        IPaymentProcessor proc = processorMap.get(processor);
        log.info("[getProcessor] processor : {}", proc);
        if (proc == null) {
            throw new IllegalArgumentException("Procesador no soportado: " + processor);
        }
        return proc;
    }
}
