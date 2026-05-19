package com.pe.limon.api.transactions.payment.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PaymentResult<T> {
    private boolean success;
    private String message;
    private String txStatus;
    private String processorRef;
    private String processorStatus;
    private T data;

    public PaymentResult(boolean success, String message, String txStatus, String processorRef, String processorStatus, T data) {
        this.success = success;
        this.message = message;
        this.txStatus = txStatus;
        this.processorRef = processorRef;
        this.processorStatus = processorStatus;
        this.data = data;
    }

    public PaymentResult(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }


    public static PaymentResult<?> success(String message) {
        return new PaymentResult<>(true, message,null);
    }

    public static PaymentResult<?> failure(String message) {
        return new PaymentResult<>(false, message,null);
    }

    // Getters y Setters
}
