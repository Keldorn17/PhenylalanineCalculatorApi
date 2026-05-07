package com.keldorn.phenylalaninecalculatorapi.dto.auth;

import jakarta.validation.constraints.NotBlank;

import lombok.Builder;

@Builder
public record AuthUsernameChangeRequest(@NotBlank String username, @NotBlank String password) {
}
