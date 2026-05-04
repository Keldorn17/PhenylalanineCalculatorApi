package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.exception.InvalidJwtTokenReceivedException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class JwtServiceTests {

    private static final String SECRET_A =
            "VGhpc0lzQVNlY3JldEtleUZvclRlc3RpbmdUaGF0SXNMb25nRW5vdWdoMTIzNDU2";
    private static final String SECRET_B =
            "VSwsajBDcVlMRzJ8Lik9ekUhJTxgUyYpfU1gfjxJIT5KZVpqRUFHS1Y+WUU4J0l1XEZLP2gwTTVJ";

    private final JwtService jwtServiceA = new JwtService(SECRET_A);
    private final JwtService jwtServiceB = new JwtService(SECRET_B);

    private final String TEST_USERNAME = "Test User";
    private final Long TEST_USER_ID = 1L;

    @Test
    public void shouldExtractUsername_whenTokenSignedWithSameKey() {
        String token = jwtServiceA.generateToken(TEST_USERNAME, TEST_USER_ID);
        String username = jwtServiceA.extractUsername(token);
        Assertions.assertThat(username).isEqualTo(TEST_USERNAME);
    }

    @Test
    public void shouldExtractUserId_whenTokenSignedWithSameKey() {
        String token = jwtServiceA.generateToken(TEST_USERNAME, TEST_USER_ID);
        Long userId = jwtServiceA.extractUserId(token);
        Assertions.assertThat(userId).isEqualTo(TEST_USER_ID);
    }

    @Test
    public void shouldThrow_whenTokenMalformed() {
        String token = "Invalid Token";
        Assertions.assertThatThrownBy(() -> jwtServiceA.extractUsername(token))
                .isInstanceOf(InvalidJwtTokenReceivedException.class);
    }

    @Test
    public void shouldThrow_whenTokenSignedWithDifferentKey() {
        String token = jwtServiceB.generateToken(TEST_USERNAME, TEST_USER_ID);
        Assertions.assertThatThrownBy(() -> jwtServiceA.extractUsername(token))
                .isInstanceOf(InvalidJwtTokenReceivedException.class);

    }

}
