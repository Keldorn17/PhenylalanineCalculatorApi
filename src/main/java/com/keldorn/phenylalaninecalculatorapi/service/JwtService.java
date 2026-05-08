package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.User;
import com.keldorn.phenylalaninecalculatorapi.exception.InvalidJwtTokenReceivedException;

import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
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

    public String generateToken(User user) {
        log.debug("Generating Token");
        final long EXPIRATION = 1000L * 60 * 60 * 24;
        return Jwts.builder()
                .subject(String.valueOf(user.getUserId()))
                .claim("username", user.getUsername())
                .claim("roles", user.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList()
                )
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(signingKey)
                .compact();
    }

    public Long extractUserId(String token) {
        log.debug("Extracting User Id.");
        try {
            return Long.parseLong(Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Error during extracting user id: {}", e.getMessage());
            throw new InvalidJwtTokenReceivedException("Invalid accessToken received");
        }
    }

    public List<String> extractRoles(String token) {
        log.debug("Extracting Roles.");
        try {
            List<?> roles = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("roles", List.class);
            return roles != null ? roles.stream()
                                   .filter(String.class::isInstance)
                                   .map(String.class::cast)
                                   .toList() : List.of();
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Error during extracting roles: {}", e.getMessage());
            throw new InvalidJwtTokenReceivedException("Invalid accessToken received");
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
                    .get("username", String.class);
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Error during extracting username: {}", e.getMessage());
            throw new InvalidJwtTokenReceivedException("Invalid accessToken received");
        }
    }

}
