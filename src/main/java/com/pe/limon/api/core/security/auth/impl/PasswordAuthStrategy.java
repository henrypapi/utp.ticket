package com.pe.limon.api.core.security.auth.impl;

import com.pe.limon.api.gateway.auth.dto.AuthLoginRequest;
import com.pe.limon.api.core.security.auth.AuthProvider;
import com.pe.limon.api.core.security.auth.AuthStrategy;
import com.pe.limon.api.transactions.authz.repository.entity.UserEntity;
import com.pe.limon.api.transactions.authz.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PasswordAuthStrategy implements AuthStrategy {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @Override public AuthProvider type() { return AuthProvider.PASSWORD; }

    @Override
    public UserEntity authenticate(AuthLoginRequest req) {
        if (req.email() == null || req.password() == null) {
            throw new BadCredentialsException("Email/password missing");
        }
        var user = userRepo.findByEmail(req.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        log.debug("[authenticate] User : {} ",user);
        if (user.getPassword() == null || !passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        return user;
    }
}
