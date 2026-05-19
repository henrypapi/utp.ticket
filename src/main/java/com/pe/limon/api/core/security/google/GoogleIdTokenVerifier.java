package com.pe.limon.api.core.security.google;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Component
public class GoogleIdTokenVerifier {

    private final JwtDecoder googleJwtDecoder;

    public GoogleIdTokenVerifier(
            @Value("${google.jwk-set-uri}") String jwkSetUri,
            @Value("${google.issuer}") String issuer,
            @Value("${spring.security.oauth2.client.registration.google.client-id}") String clientId) {

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> customValidator = (jwt) -> {
            if (jwt.getAudience() == null || !jwt.getAudience().contains(clientId)) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Invalid audience", null));
            }
            Boolean emailVerified = jwt.getClaim("email_verified");
            if (emailVerified != null && !emailVerified) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Email not verified", null));
            }
            return OAuth2TokenValidatorResult.success();
        };

        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(withIssuer, customValidator);

        decoder.setJwtValidator(validator);

        this.googleJwtDecoder = decoder;
    }

    public Jwt decode(String idToken) {
        return googleJwtDecoder.decode(idToken);
    }
}
