package com.keldorn.phenylalaninecalculatorapi.dto.auth;

import jakarta.validation.constraints.NotBlank;

import lombok.Builder;

@Builder
public record AuthRequest(@NotBlank String username, @NotBlank String password) {}
