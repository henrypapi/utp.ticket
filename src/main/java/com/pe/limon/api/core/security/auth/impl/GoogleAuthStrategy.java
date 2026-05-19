package com.pe.limon.api.core.security.auth.impl;

import com.pe.limon.api.gateway.auth.dto.AuthLoginRequest;
import com.pe.limon.api.core.security.auth.AuthProvider;
import com.pe.limon.api.core.security.auth.AuthStrategy;
import com.pe.limon.api.core.security.google.GoogleIdTokenVerifier;
import com.pe.limon.api.transactions.authz.repository.entity.UserEntity;
import com.pe.limon.api.transactions.authz.business.user.UserRegisterService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoogleAuthStrategy implements AuthStrategy {

    private final GoogleIdTokenVerifier googleVerifier;
    private final UserRegisterService userRegisterService;

    @Override public AuthProvider type() { return AuthProvider.GOOGLE; }

    @Override
    public UserEntity authenticate(AuthLoginRequest req) {
        if (req.idToken() == null) {
            throw new BadCredentialsException("Google idToken is required");
        }
        var idJwt = googleVerifier.decode(req.idToken());
        var email   = idJwt.getClaimAsString("email");

        return userRegisterService.upsertGoogleUser(email);
    }
}
