package com.keldorn.phenylalaninecalculatorapi.service;

import com.github.benmanes.caffeine.cache.Cache;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class TokenDenylistService {

    private final Cache<Long, Boolean> tokenDenylistCache;

    public TokenDenylistService(@Qualifier("tokenDenylistCache") Cache<Long, Boolean> tokenDenylistCache) {
        this.tokenDenylistCache = tokenDenylistCache;
    }

    public void revokeToken(Long userId) {
        if (userId != null) {
            tokenDenylistCache.put(userId, Boolean.TRUE);
        }
    }

    public boolean isRevoked(Long userId) {
        if (userId == null) {
            return false;
        }
        return tokenDenylistCache.getIfPresent(userId) != null;
    }

}
