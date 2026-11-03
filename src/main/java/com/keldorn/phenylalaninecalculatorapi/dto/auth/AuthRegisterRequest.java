package com.keldorn.phenylalaninecalculatorapi.dto.auth;

import com.keldorn.phenylalaninecalculatorapi.annotation.CheckPasswordStrength;
import com.keldorn.phenylalaninecalculatorapi.annotation.CheckUsername;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.Builder;

@Builder
public record AuthRegisterRequest(@NotBlank @Email String email, @NotBlank @CheckUsername String username,
                                  @NotBlank @CheckPasswordStrength String password) {}
