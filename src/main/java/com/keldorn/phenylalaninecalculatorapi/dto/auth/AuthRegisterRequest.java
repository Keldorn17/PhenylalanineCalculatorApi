package com.keldorn.phenylalaninecalculatorapi.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.Builder;

@Builder
public record AuthRegisterRequest(@NotBlank @Email String email, @NotBlank String username,
                                  @NotBlank @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])" +
                                          "[A-Za-z\\d@$!%*?&]{8,}$") String password) {}
