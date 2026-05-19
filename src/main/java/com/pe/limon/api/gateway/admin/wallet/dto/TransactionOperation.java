package com.pe.limon.api.gateway.admin.wallet.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TransactionOperation {
    C("DEPOSITO"),
    D("RETIRO");

    private final String description;

    TransactionOperation(String description) {
        this.description = description;
    }
    @JsonValue
    public String getDescription() {
        return description;
    }
}