package com.pe.limon.api.transactions.orders.business.orders.model;

import lombok.Getter;

@Getter
public enum OrderStatusEnum {

    /** Orden creada, aún no se inicia el pago */
    CREATED("N"),
    /** Pago iniciado, pendiente de confirmación del procesador */
    WAITING_PROCESSOR("W"),
    /** Pago confirmado por el procesador */
    PAID("P"),
    /** Tickets generados correctamente */
    COMPLETED("C"),
    /** Pago OK pero falló la generación de tickets o error general*/
    FULFILLMENT_FAILED("E"),
    /** Error genérico inesperado del flujo */
    ERROR("X"),
    /** Error genérico inesperado del flujo */
    REJECTED("R"),
    /** Pago rechazado / cancelado / expirado */
    PAYMENT_FAILED("F");

    private final String code;

    OrderStatusEnum(String code) {
        this.code = code;
    }

    /* =======================
       Helpers de dominio
       ======================= */

    /** ¿El pago ya fue confirmado por el procesador? */
    public boolean isPaid() {
        return this == PAID || this == COMPLETED || this == FULFILLMENT_FAILED;
    }

    /** ¿La orden está cerrada (no debería seguir avanzando)? */
    public boolean isFinal() {
        return this == COMPLETED || this == PAYMENT_FAILED;
    }

    /** ¿Permite generar tickets? */
    public boolean canGenerateTickets() {
        return this == PAID || this == FULFILLMENT_FAILED;
    }

    /** ¿Está esperando respuesta del procesador? */
    public boolean isWaitingProcessor() {
        return this == WAITING_PROCESSOR;
    }

    /* =======================
       Factory desde DB
       ======================= */

    public static OrderStatusEnum fromCode(String code) {
        for (OrderStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown OrderStatus code: " + code);
    }
}
