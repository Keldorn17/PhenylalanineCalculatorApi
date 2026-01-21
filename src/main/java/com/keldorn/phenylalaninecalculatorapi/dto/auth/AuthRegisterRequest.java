package com.keldorn.phenylalaninecalculatorapi.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record AuthRegisterRequest(@NotNull @Email String email, @NotNull String username, @NotNull String password,
                                  @NotNull String timezone) {}
