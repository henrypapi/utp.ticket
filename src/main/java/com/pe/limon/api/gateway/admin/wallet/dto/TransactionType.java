package com.pe.limon.api.gateway.admin.wallet.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TransactionType {
    P("Abono por compra de venta"),
    W("Retiro");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }
    @JsonValue
    public String getDescription() {
        return description;
    }
}