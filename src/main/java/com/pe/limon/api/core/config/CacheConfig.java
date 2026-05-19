package com.pe.limon.api.core.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.pe.limon.api.transactions.authz.business.permissions.EffectivePerm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.benmanes.caffeine.cache.Cache;

import java.time.Duration;

@Configuration
public class CacheConfig {

    @Value("${application.cache.maximum-size}")
    private long maximumSize;

    @Value("${application.cache.expires-after-write}")
    private long expiresAfterWrite;

    @Bean
    public Cache<String, EffectivePerm> permCache() {
        // Cache genérica con TTL 20 minutos y máximo 100 mil entradas
        return Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterWrite(Duration.ofMinutes(expiresAfterWrite))
                .build();
    }
}
