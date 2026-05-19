package com.pe.limon.api.transactions.payment.processors;

import com.pe.limon.api.transactions.payment.dto.generic.GetTokenDTO;

import java.util.Optional;

public interface ISupportsFormToken {
    Optional<String> generateFormToken(GetTokenDTO request);
}