package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.User;
import com.keldorn.phenylalaninecalculatorapi.exception.InvalidJwtTokenReceivedException;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import javax.crypto.SecretKey;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Slf4j
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final Long accessExpirationTime;
    private final Long refreshExpirationTime;

    public JwtService(@Value("${jwt.secret.string}") String secretString,
            @Value("${jwt.access.expiration.time}") Long accessExpirationTime,
            @Value("${jwt.refresh.expiration.time}") Long refreshExpirationTime) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretString));
        this.accessExpirationTime = accessExpirationTime;
        this.refreshExpirationTime = refreshExpirationTime;
    }

    public long getRefreshExpirationTime() {
        return refreshExpirationTime;
    }

    public String generateAccessToken(User user) {
        log.debug("Generating Access Token");
        return generateToken(user, accessExpirationTime);
    }

    public String generateRefreshToken(User user) {
        log.debug("Generating Refresh Token");
        return generateToken(user, refreshExpirationTime);
    }

    public Long extractUserId(String token) {
        log.debug("Extracting User Id.");
        return Long.parseLong(extractClaim(token, Claims::getSubject));
    }

    public List<String> extractRoles(String token) {
        log.debug("Extracting Roles.");
        List<?> roles = extractClaim(token, claims -> claims.get("roles", List.class));
        return roles != null ? roles.stream()
                               .filter(String.class::isInstance)
                               .map(String.class::cast)
                               .toList() : List.of();
    }

    public String extractUsername(String token) {
        log.debug("Extracting Username.");
        return extractClaim(token, claims -> claims.get("username", String.class));
    }

    private String generateToken(User user, Long expirationTime) {
        log.debug("Generating Token");
        var authorities = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(user.getUserId()))
                .claim("username", user.getUsername())
                .claim("roles", authorities)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(signingKey)
                .compact();
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Error during extracting claims: {}", e.getMessage());
            throw new InvalidJwtTokenReceivedException("Invalid token received");
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

}
