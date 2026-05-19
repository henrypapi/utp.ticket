package com.pe.limon.api.core.security.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthStrategyFactory {
    private final Map<AuthProvider, AuthStrategy> registry = new EnumMap<>(AuthProvider.class);

    @Autowired
    public AuthStrategyFactory(List<AuthStrategy> strategies) {
        strategies.forEach(s -> registry.put(s.type(), s));
        log.info("Auth strategies registradas: count={}, keys={}", strategies.size(), registry.keySet());
    }

    public AuthStrategy get(AuthProvider provider) {
        log.info("registry {}",registry);
        var strat = registry.get(provider);
        if (strat == null) throw new IllegalArgumentException("Auth provider not supported: " + provider);
        return strat;
    }
}