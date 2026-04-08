package com.keldorn.phenylalaninecalculatorapi.dto.user;

import java.math.BigDecimal;

import jakarta.validation.constraints.Email;

public record UserRequest(@Email String email, BigDecimal dailyLimit) {}
