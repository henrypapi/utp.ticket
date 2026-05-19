package com.pe.limon.api.gateway.client.users.personal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record PersonalInfoDTO(
    @NotBlank String firstName,
    @NotBlank String lastName,
    String gender,
    String department,
    String province,
    String district,
    String address,
    @NotBlank String phoneNumber,
    @NotBlank String documentType,
    @NotBlank String documentNumber,
    @NotBlank LocalDate birthDate
) {}