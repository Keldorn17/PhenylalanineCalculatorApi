package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.config.JwtProperties;
import com.keldorn.phenylalaninecalculatorapi.domain.entity.User;
import com.keldorn.phenylalaninecalculatorapi.exception.InvalidJwtTokenReceivedException;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

import lombok.extern.slf4j.Slf4j;

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

    private static final String ACCESS_TYPE = "ACCESS";
    private static final String REFRESH_TYPE = "REFRESH";

    private final SecretKey signingKeyAccess;
    private final SecretKey signingKeyRefresh;
    private final Long accessExpirationTime;
    private final Long refreshExpirationTime;

    public JwtService(JwtProperties jwtProperties) {
        this.signingKeyAccess = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecret().getAccess()));
        this.signingKeyRefresh = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecret().getRefresh()));
        this.accessExpirationTime = jwtProperties.getAccess().getExpirationTime().toMillis();
        this.refreshExpirationTime = jwtProperties.getRefresh().getExpirationTime().toMillis();
    }

    public long getRefreshExpirationTime() {
        return refreshExpirationTime;
    }

    public String generateAccessToken(User user) {
        log.debug("Generating Access Token");
        return generateToken(user, accessExpirationTime, this.signingKeyAccess, ACCESS_TYPE);
    }

    public String generateRefreshToken(User user) {
        log.debug("Generating Refresh Token");
        return generateToken(user, refreshExpirationTime, this.signingKeyRefresh, REFRESH_TYPE);
    }

    public Claims validateAndParseAccessToken(String token) {
        return extractAccessClaims(token);
    }

    public Long extractUserId(Claims claims) {
        log.debug("Extracting User Id.");
        return Long.parseLong(claims.getSubject());
    }

    public List<String> extractRoles(Claims claims) {
        log.debug("Extracting Roles.");
        List<?> roles = claims.get("roles", List.class);
        return roles != null ? roles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .toList() : List.of();
    }

    public String extractUsername(Claims claims) {
        log.debug("Extracting Username.");
        return claims.get("username", String.class);
    }

    private String generateToken(User user, Long expirationTime, SecretKey signingKey, String tokenType) {
        log.debug("Generating Token");
        var authorities = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(user.getUserId()))
                .claim("username", user.getUsername())
                .claim("roles", authorities)
                .claim("type", tokenType)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(signingKey)
                .compact();
    }

    private Claims extractAllClaims(String token, SecretKey signingKey) {
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

    private Claims extractAccessClaims(String token) {
        Claims claims = extractAllClaims(token, this.signingKeyAccess);
        if (!ACCESS_TYPE.equals(claims.get("type", String.class))) {
            throw new InvalidJwtTokenReceivedException("Invalid token type");
        }
        return claims;
    }

    public Long extractUserIdFromRefreshToken(String token) {
        log.debug("Extracting User Id from Refresh Token.");
        Claims claims = extractAllClaims(token, this.signingKeyRefresh);
        if (!REFRESH_TYPE.equals(claims.get("type", String.class))) {
            throw new InvalidJwtTokenReceivedException("Invalid token type");
        }
        return Long.parseLong(claims.getSubject());
    }

}
