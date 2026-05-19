package com.pe.limon.api.transactions.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record PaymentAnswerDTO (
    @NotNull
    @JsonProperty("kr-hash")
    String krHash,
    @NotNull
    @JsonProperty("kr-answer")
    String krAnswer){
}
