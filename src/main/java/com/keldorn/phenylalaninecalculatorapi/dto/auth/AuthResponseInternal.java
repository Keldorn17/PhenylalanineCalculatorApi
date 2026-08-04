package com.keldorn.phenylalaninecalculatorapi.dto.auth;

import lombok.Builder;

@Builder
public record AuthResponseInternal(String accessToken, String refreshToken) {}
