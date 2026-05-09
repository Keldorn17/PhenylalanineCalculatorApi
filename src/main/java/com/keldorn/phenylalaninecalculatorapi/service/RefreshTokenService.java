package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.RefreshToken;
import com.keldorn.phenylalaninecalculatorapi.domain.entity.User;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthResponse;
import com.keldorn.phenylalaninecalculatorapi.exception.InvalidJwtTokenReceivedException;
import com.keldorn.phenylalaninecalculatorapi.repository.RefreshTokenRepository;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public String save(User user) {
        log.debug("Saving Refresh Token.");
        var token = jwtService.generateRefreshToken(user);
        refreshTokenRepository.deleteByUser_UserId(user.getUserId());
        var refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiryDate(
                        ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(jwtService.getRefreshExpirationTime() / 1000))
                .build();
        refreshTokenRepository.save(refreshToken);
        return token;
    }

    public AuthResponse refresh(String refreshToken, User user) {
        log.debug("Creating access token from refresh token");
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidJwtTokenReceivedException("Invalid token received"));
        if (token.getExpiryDate().isBefore(ZonedDateTime.now(ZoneOffset.UTC))) {
            refreshTokenRepository.delete(token);
            throw new InvalidJwtTokenReceivedException("Refresh token was expired");
        }
        String accessToken = jwtService.generateAccessToken(user);
        return new AuthResponse(accessToken, refreshToken);
    }

}
