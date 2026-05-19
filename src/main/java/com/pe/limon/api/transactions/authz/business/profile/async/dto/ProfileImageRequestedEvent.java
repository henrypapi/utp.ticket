package com.pe.limon.api.transactions.authz.business.profile.async.dto;

public record ProfileImageRequestedEvent(String fileName,byte[] bytes) {}
