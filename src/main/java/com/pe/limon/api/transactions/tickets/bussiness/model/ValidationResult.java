package com.pe.limon.api.transactions.tickets.bussiness.model;

public record ValidationResult(boolean forbidden, String message, Long timestamp) {}
