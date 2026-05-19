package com.pe.limon.api.gateway.auth.dto;

import com.pe.limon.api.core.security.auth.AuthProvider;
import jakarta.validation.constraints.NotNull;



public record AuthLoginRequest(
        @NotNull AuthProvider provider,
        String email,       // usado por PASSWORD (y opcionalmente por otros)
        String password,    // usado por PASSWORD
        String idToken,     // usado por GOOGLE
        Boolean rememberDevice,   // <-- NUEVO
        String deviceId           // <-- NUEVO (en header o body)
) {}
