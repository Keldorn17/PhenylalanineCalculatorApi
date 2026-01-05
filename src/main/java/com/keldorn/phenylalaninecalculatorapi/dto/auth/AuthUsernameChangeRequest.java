package com.keldorn.phenylalaninecalculatorapi.dto.auth;

import jakarta.validation.constraints.NotNull;

public record AuthUsernameChangeRequest(@NotNull String username, @NotNull String password) {
}
