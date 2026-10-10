package com.keldorn.phenylalaninecalculatorapi.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SecurityCacheConfig {

    private final JwtProperties jwtProperties;

    @Bean("tokenDenylistCache")
    public Cache<Long, Boolean> tokenDenylistCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(jwtProperties.getAccess().getExpirationTime())
                .maximumSize(50_000)
                .build();
    }

}
