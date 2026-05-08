package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.User;
import com.keldorn.phenylalaninecalculatorapi.domain.enums.Role;
import com.keldorn.phenylalaninecalculatorapi.exception.InvalidJwtTokenReceivedException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class JwtServiceTests {

    private static final String SECRET_A =
            "VGhpc0lzQVNlY3JldEtleUZvclRlc3RpbmdUaGF0SXNMb25nRW5vdWdoMTIzNDU2";
    private static final String SECRET_B =
            "VSwsajBDcVlMRzJ8Lik9ekUhJTxgUyYpfU1gfjxJIT5KZVpqRUFHS1Y+WUU4J0l1XEZLP2gwTTVJ";

    private final JwtService jwtServiceA = new JwtService(SECRET_A);
    private final JwtService jwtServiceB = new JwtService(SECRET_B);

    private final String testUsername = "Test User";
    private final Long testUserId = 1L;
    private final User testUser = User.builder().userId(testUserId).role(Role.ROLE_USER).username(testUsername).build();

    @Test
    void shouldExtractUsername_whenTokenSignedWithSameKey() {
        String token = jwtServiceA.generateToken(testUser);
        String username = jwtServiceA.extractUsername(token);
        Assertions.assertThat(username).isEqualTo(testUsername);
    }

    @Test
    void shouldExtractUserId_whenTokenSignedWithSameKey() {
        String token = jwtServiceA.generateToken(testUser);
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
        String token = jwtServiceB.generateToken(testUser);
        Assertions.assertThatThrownBy(() -> jwtServiceA.extractUsername(token))
                .isInstanceOf(InvalidJwtTokenReceivedException.class);

    }

}
