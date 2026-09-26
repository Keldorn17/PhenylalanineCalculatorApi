package com.keldorn.phenylalaninecalculatorapi.dto.auth;

import com.keldorn.phenylalaninecalculatorapi.annotation.CheckPasswordStrength;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.Builder;

@Builder
public record AuthRegisterRequest(@NotBlank @Email String email, @NotBlank String username,
                                  @NotBlank @CheckPasswordStrength String password) {}
