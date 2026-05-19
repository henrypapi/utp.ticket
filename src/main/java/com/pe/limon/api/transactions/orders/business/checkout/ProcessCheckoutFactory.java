package com.pe.limon.api.transactions.orders.business.checkout;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class ProcessCheckoutFactory {
    private final Map<String, IProcessCheckout> processCheckoutMap;

    public ProcessCheckoutFactory(Map<String, IProcessCheckout> processorMap) {
        this.processCheckoutMap = processorMap;
    }

    public IProcessCheckout processCheckout(String processor) {
        log.info("[getProcessor] processCheckoutMap : {}", processCheckoutMap);
        IProcessCheckout proc = processCheckoutMap.get(processor);
        log.info("[getProcessor] processor : {}", proc);
        if (proc == null) {
            throw new IllegalArgumentException("Procesador no soportado: " + processor);
        }
        return proc;
    }
}
