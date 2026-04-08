package com.keldorn.phenylalaninecalculatorapi.dto.user;

import java.math.BigDecimal;

public record UserResponse(Long id, String username, String email, BigDecimal dailyLimit) {}
