package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.exception.unauthorized.InvalidJwtTokenReceivedException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Service
public class JwtService {

    private final SecretKey signingKey;

    public JwtService(@Value("${jwt.secret.string}") String secretString) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretString));;
    }

    public String generateToken(String username) {
        log.debug("Generating Token");
        final long EXPIRATION = 1000 * 60 * 60 * 24;
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(signingKey)
                .compact();
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
