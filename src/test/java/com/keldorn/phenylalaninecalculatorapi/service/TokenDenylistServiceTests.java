package com.keldorn.phenylalaninecalculatorapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.benmanes.caffeine.cache.Cache;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TokenDenylistServiceTests {

    @Mock
    private Cache<Long, Boolean> tokenDenylistCache;

    @InjectMocks
    private TokenDenylistService tokenDenylistService;

    @Test
    void revokeToken_whenUserIdNotNull_shouldPutInCache() {
        Long userId = 1L;
        tokenDenylistService.revokeToken(userId);
        verify(tokenDenylistCache).put(userId, Boolean.TRUE);
    }

    @Test
    void revokeToken_whenUserIdNull_shouldDoNothing() {
        tokenDenylistService.revokeToken(null);
        verify(tokenDenylistCache, never()).put(null, Boolean.TRUE);
    }

    @Test
    void isRevoked_whenUserIdNull_shouldReturnFalse() {
        assertThat(tokenDenylistService.isRevoked(null)).isFalse();
        verify(tokenDenylistCache, never()).getIfPresent(null);
    }

    @Test
    void isRevoked_whenUserIdRevoked_shouldReturnTrue() {
        Long userId = 1L;
        when(tokenDenylistCache.getIfPresent(userId)).thenReturn(Boolean.TRUE);
        assertThat(tokenDenylistService.isRevoked(userId)).isTrue();
    }

    @Test
    void isRevoked_whenUserIdNotRevoked_shouldReturnFalse() {
        Long userId = 1L;
        when(tokenDenylistCache.getIfPresent(userId)).thenReturn(null);
        assertThat(tokenDenylistService.isRevoked(userId)).isFalse();
    }

}
