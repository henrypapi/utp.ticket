package com.pe.limon.api.transactions.notifications.email.dto;

public record EmailAttachment(
        String filename,
        byte[] content,
        String contentType,
        boolean inline,          // true = Content-ID inline
        String contentId         // e.g. "logo" para <img src="cid:logo">
) {}
