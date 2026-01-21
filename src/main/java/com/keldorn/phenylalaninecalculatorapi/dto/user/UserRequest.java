package com.keldorn.phenylalaninecalculatorapi.dto.user;

import jakarta.validation.constraints.Email;

import java.math.BigDecimal;

public record UserRequest(@Email String email, BigDecimal dailyLimit, String timezone) {}
