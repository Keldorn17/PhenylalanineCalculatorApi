package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.User;
import com.keldorn.phenylalaninecalculatorapi.exception.InvalidJwtTokenReceivedException;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class JwtServiceTests {

    private static final String SECRET_A =
            "VGhpc0lzQVNlY3JldEtleUZvclRlc3RpbmdUaGF0SXNMb25nRW5vdWdoMTIzNDU2";
    private static final String SECRET_B =
            "VSwsajBDcVlMRzJ8Lik9ekUhJTxgUyYpfU1gfjxJIT5KZVpqRUFHS1Y+WUU4J0l1XEZLP2gwTTVJ";
    private static final Long EXPIRATION_TIME = 300000L;

    private final JwtService jwtServiceA = new JwtService(SECRET_A, EXPIRATION_TIME, EXPIRATION_TIME);
    private final JwtService jwtServiceB = new JwtService(SECRET_B, EXPIRATION_TIME, EXPIRATION_TIME);

    private final String testUsername = "Test User";
    private final Long testUserId = 1L;
    private final User testUser =
            User.builder().userId(testUserId).roles(List.of(TestEntityFactory.role())).username(testUsername).build();

    @Test
    void shouldExtractUsername_whenTokenSignedWithSameKey() {
        String token = jwtServiceA.generateAccessToken(testUser);
        String username = jwtServiceA.extractUsername(token);
        Assertions.assertThat(username).isEqualTo(testUsername);
    }

    @Test
    void shouldExtractUserId_whenTokenSignedWithSameKey() {
        String token = jwtServiceA.generateAccessToken(testUser);
        Long userId = jwtServiceA.extractUserId(token);
        Assertions.assertThat(userId).isEqualTo(testUserId);
    }

    @Test
    void shouldThrow_whenTokenMalformed() {
        String token = "Invalid Token";
        Assertions.assertThatThrownBy(() -> jwtServiceA.extractUsername(token))
                .isInstanceOf(InvalidJwtTokenReceivedException.class);
    }

    @Test
    void shouldThrow_whenTokenSignedWithDifferentKey() {
        String token = jwtServiceB.generateAccessToken(testUser);
        Assertions.assertThatThrownBy(() -> jwtServiceA.extractUsername(token))
                .isInstanceOf(InvalidJwtTokenReceivedException.class);

    }

}
