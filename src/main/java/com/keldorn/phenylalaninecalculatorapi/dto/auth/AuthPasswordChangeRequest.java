package com.keldorn.phenylalaninecalculatorapi.dto.auth;

import jakarta.validation.constraints.NotBlank;

import lombok.Builder;

@Builder
public record AuthPasswordChangeRequest(@NotBlank String oldPassword, @NotBlank String password) {}
