package com.pe.limon.api.core.utils.enums;

import com.pe.limon.api.transactions.orders.business.orders.model.OrderStatusEnum;
import lombok.Getter;

@Getter
public enum StatusEnum {
    /** Orden creada, aún no se inicia el pago */
    ACTIVE("A"),
    /** Pago iniciado, pendiente de confirmación del procesador */
    INACTIVE("I");

    private final String code;

    StatusEnum(String code) {
        this.code = code;
    }

    /* =======================
       Helpers de dominio
       ======================= */

    public static StatusEnum fromCode(String code) {
        for (StatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown OrderStatus code: " + code);
    }
}
