package com.pe.limon.api.transactions.payment.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CustomerDTO (
    @NotNull
    @NotBlank
    String name,
    @NotNull
    @NotBlank
    String lastname,
    @NotNull
    @NotBlank
    String address,
    @NotNull
    @NotBlank
    String district,
    @NotNull
    @NotBlank
    String phoneNumber,
    @NotNull
    @NotBlank
    @Email
    String email){
}
