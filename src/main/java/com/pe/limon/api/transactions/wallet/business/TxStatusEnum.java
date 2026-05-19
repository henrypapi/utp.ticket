package com.pe.limon.api.transactions.wallet.business;

import com.pe.limon.api.transactions.events.business.PaymentModeEnum;
import lombok.Getter;

@Getter
public enum TxStatusEnum {
    /** Orden creada, aún no se inicia el pago */
     COMPLETED("C"),
    /** Pago iniciado, pendiente de confirmación del procesador */
    PENDING("P"),
    /** Pago confirmado por el procesador */
    REJECTED("R");

    private final String code;

    TxStatusEnum(String code) {
        this.code = code;
    }

    public static TxStatusEnum fromCode(String code) {
        for (TxStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown OrderStatus code: " + code);
    }
}
