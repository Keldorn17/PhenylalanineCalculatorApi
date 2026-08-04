package com.keldorn.phenylalaninecalculatorapi.utils;

import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;

import java.time.Duration;

import lombok.experimental.UtilityClass;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

@UtilityClass
public class HeaderUtils {

    public HttpHeaders getRefreshHeader(String refreshToken, Duration maxAge) {
        HttpHeaders httpHeaders = new HttpHeaders();
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .path(ApiRoutes.REFRESH_PATH)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .maxAge(maxAge)
                .build();
        httpHeaders.add(HttpHeaders.SET_COOKIE, cookie.toString());
        return httpHeaders;
    }

}
