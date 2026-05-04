package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.exception.InvalidJwtTokenReceivedException;

import java.util.Date;

import javax.crypto.SecretKey;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Slf4j
@Service
public class JwtService {

    private final SecretKey signingKey;

    public JwtService(@Value("${jwt.secret.string}") String secretString) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretString));
    }

    public String generateToken(String username, Long userId) {
        log.debug("Generating Token");
        final long EXPIRATION = 1000 * 60 * 60 * 24;
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(signingKey)
                .compact();
    }

    public Long extractUserId(String token) {
        log.debug("Extracting User Id.");
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("userId", Long.class);
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Error during extracting user id: {}", e.getMessage());
            throw new InvalidJwtTokenReceivedException("Invalid token received");
        }
    }

    public String extractUsername(String token) {
        log.debug("Extracting Username.");
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Error during extracting username: {}", e.getMessage());
            throw new InvalidJwtTokenReceivedException("Invalid token received");
        }
    }

}
