package com.pe.limon.api.transactions.notifications.email.enums;

public enum EmailTemplate {
    PURCHASE_RECEIPT("purchase-receipt"),
    TICKET_DELIVERY("ticket-delivery"),
    RESET_PASSWORD("reset-password"),
    VERIFY_EMAIL("verify-email"),
    GENERIC_INFO("generic-info");

    private final String key;
    EmailTemplate(String key) { this.key = key; }
    public String key() { return key; }
}