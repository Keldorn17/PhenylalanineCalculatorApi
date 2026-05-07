package com.keldorn.phenylalaninecalculatorapi.dto.user;

import java.math.BigDecimal;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.Builder;

@Builder
public record UserRequest(@Email String email, @PositiveOrZero BigDecimal dailyLimit) {}
