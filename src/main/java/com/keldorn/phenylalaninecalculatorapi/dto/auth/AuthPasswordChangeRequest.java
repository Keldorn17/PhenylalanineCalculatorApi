package com.keldorn.phenylalaninecalculatorapi.dto.auth;

import com.keldorn.phenylalaninecalculatorapi.annotation.CheckPasswordStrength;

import jakarta.validation.constraints.NotBlank;

import lombok.Builder;

@Builder
public record AuthPasswordChangeRequest(@NotBlank String oldPassword,
                                        @NotBlank @CheckPasswordStrength String password) {}
