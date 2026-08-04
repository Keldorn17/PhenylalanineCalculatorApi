package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.config.JwtProperties;
import com.keldorn.phenylalaninecalculatorapi.domain.entity.User;
import com.keldorn.phenylalaninecalculatorapi.exception.InvalidJwtTokenReceivedException;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;

import java.time.Duration;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;

class JwtServiceTests {

    private static final String SECRET_A =
            "VGhpc0lzQVNlY3JldEtleUZvclRlc3RpbmdUaGF0SXNMb25nRW5vdWdoMTIzNDU2";
    private static final String SECRET_B =
            "VSwsajBDcVlMRzJ8Lik9ekUhJTxgUyYpfU1gfjxJIT5KZVpqRUFHS1Y+WUU4J0l1XEZLP2gwTTVJ";
    private static final Duration EXPIRATION_TIME = Duration.ofDays(1);

    private final JwtService jwtServiceA =
            new JwtService(createJwtProperties(SECRET_A, SECRET_A, EXPIRATION_TIME, EXPIRATION_TIME));
    private final JwtService jwtServiceB =
            new JwtService(createJwtProperties(SECRET_B, SECRET_B, EXPIRATION_TIME, EXPIRATION_TIME));

    private final String testUsername = "Test User";
    private final Long testUserId = 1L;
    private final User testUser =
            User.builder().userId(testUserId).roles(List.of(TestEntityFactory.role())).username(testUsername).build();

    private static JwtProperties createJwtProperties(String accessSecret, String refreshSecret, Duration accessExp,
            Duration refreshExp) {
        JwtProperties properties = new JwtProperties();
        JwtProperties.Secret secret = new JwtProperties.Secret();
        secret.setAccess(accessSecret);
        secret.setRefresh(refreshSecret);
        properties.setSecret(secret);
        JwtProperties.Access access = new JwtProperties.Access();
        access.setExpirationTime(accessExp);
        properties.setAccess(access);
        JwtProperties.Refresh refresh = new JwtProperties.Refresh();
        refresh.setExpirationTime(refreshExp);
        properties.setRefresh(refresh);
        return properties;
    }

    @Test
    void shouldExtractUsername_whenTokenSignedWithSameKey() {
        String token = jwtServiceA.generateAccessToken(testUser);
        Claims claims = jwtServiceA.validateAndParseAccessToken(token);
        String username = jwtServiceA.extractUsername(claims);
        Assertions.assertThat(username).isEqualTo(testUsername);
    }

    @Test
    void shouldExtractUserId_whenTokenSignedWithSameKey() {
        String token = jwtServiceA.generateAccessToken(testUser);
        Claims claims = jwtServiceA.validateAndParseAccessToken(token);
        Long userId = jwtServiceA.extractUserId(claims);
        Assertions.assertThat(userId).isEqualTo(testUserId);
    }

    @Test
    void shouldThrow_whenTokenMalformed() {
        String token = "Invalid Token";
        Assertions.assertThatThrownBy(() -> jwtServiceA.validateAndParseAccessToken(token))
                .isInstanceOf(InvalidJwtTokenReceivedException.class);
    }

    @Test
    void shouldThrow_whenTokenSignedWithDifferentKey() {
        String token = jwtServiceB.generateAccessToken(testUser);
        Assertions.assertThatThrownBy(() -> jwtServiceA.validateAndParseAccessToken(token))
                .isInstanceOf(InvalidJwtTokenReceivedException.class);

    }

}
