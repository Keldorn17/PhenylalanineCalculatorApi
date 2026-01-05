package com.keldorn.phenylalaninecalculatorapi.dto.auth;

import jakarta.validation.constraints.NotNull;

public record AuthPasswordChangeRequest(@NotNull String oldPassword, @NotNull String password) {}
