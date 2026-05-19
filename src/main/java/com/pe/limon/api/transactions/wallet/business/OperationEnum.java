package com.pe.limon.api.transactions.wallet.business;

import com.pe.limon.api.transactions.orders.business.orders.model.OrderStatusEnum;

public enum OperationEnum {
    /** Orden creada, aún no se inicia el pago */
    CREDIT("C"),
    /** Pago iniciado, pendiente de confirmación del procesador */
    DEBIT("D");

    private final String code;

    OperationEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }


    public static OperationEnum fromCode(String code) {
        for (OperationEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown Operation code: " + code);
    }
}
