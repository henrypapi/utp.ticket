package com.pe.limon.api.core.security.auth;

import com.pe.limon.api.gateway.auth.dto.AuthLoginRequest;
import com.pe.limon.api.transactions.authz.repository.entity.UserEntity;

public interface AuthStrategy {
    AuthProvider type();
    UserEntity authenticate(AuthLoginRequest request);
}
