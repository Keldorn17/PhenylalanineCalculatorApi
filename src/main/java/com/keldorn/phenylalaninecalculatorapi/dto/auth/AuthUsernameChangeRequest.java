package com.keldorn.phenylalaninecalculatorapi.dto.auth;

import com.keldorn.phenylalaninecalculatorapi.annotation.CheckUsername;

import jakarta.validation.constraints.NotBlank;

import lombok.Builder;

@Builder
public record AuthUsernameChangeRequest(@NotBlank @CheckUsername String username, @NotBlank String password) {
}
