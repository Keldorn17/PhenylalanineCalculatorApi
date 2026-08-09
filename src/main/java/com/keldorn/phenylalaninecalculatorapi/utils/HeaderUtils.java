package com.keldorn.phenylalaninecalculatorapi.utils;

import com.keldorn.phenylalaninecalculatorapi.config.RefreshCookieProperties;
import com.keldorn.phenylalaninecalculatorapi.config.JwtProperties;
import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;

import java.time.Duration;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HeaderUtils {

    private final RefreshCookieProperties refreshCookieProperties;
    private final JwtProperties jwtProperties;

    public HttpHeaders getRefreshHeader(String refreshToken) {
        return createCookieHeader(refreshToken, jwtProperties.getRefresh().getExpirationTime());
    }

    public HttpHeaders getCleanRefreshHeader() {
        return createCookieHeader("", Duration.ZERO);
    }

    private HttpHeaders createCookieHeader(String value, Duration maxAge) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", value)
                .path(ApiRoutes.AUTH_PATH)
                .httpOnly(refreshCookieProperties.isHttpOnly())
                .secure(refreshCookieProperties.isSecure())
                .sameSite(refreshCookieProperties.getSameSite().getValue())
                .maxAge(maxAge)
                .build();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.SET_COOKIE, cookie.toString());
        return httpHeaders;
    }

}
