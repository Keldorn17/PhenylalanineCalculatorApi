package com.keldorn.phenylalaninecalculatorapi.dto.auth;

import jakarta.validation.constraints.NotBlank;

import lombok.Builder;

@Builder
public record AuthRefreshRequest(@NotBlank String refreshToken) {}
