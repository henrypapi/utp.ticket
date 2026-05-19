package com.pe.limon.api.transactions.events.business;

import com.pe.limon.api.transactions.orders.business.orders.model.OrderStatusEnum;
import lombok.Getter;

@Getter
public enum PaymentModeEnum {
    /** Orden creada, aún no se inicia el pago */
    VOUCHER("V"),
    /** Pago iniciado, pendiente de confirmación del procesador */
    PROCESSOR("P"),
    /** Pago confirmado por el procesador */
    NONE("N");

    private final String code;

    PaymentModeEnum(String code) {
        this.code = code;
    }

    public static PaymentModeEnum fromCode(String code) {
        for (PaymentModeEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown OrderStatus code: " + code);
    }
}

