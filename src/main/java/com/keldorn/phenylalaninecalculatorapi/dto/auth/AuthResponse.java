package com.keldorn.phenylalaninecalculatorapi.dto.auth;

import lombok.Builder;

@Builder
public record AuthResponse(String token) {}
