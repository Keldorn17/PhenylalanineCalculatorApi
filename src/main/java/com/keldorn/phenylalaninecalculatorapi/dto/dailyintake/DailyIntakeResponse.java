package com.keldorn.phenylalaninecalculatorapi.dto.dailyintake;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;

@Builder
public record DailyIntakeResponse(Long id, LocalDate date, BigDecimal totalPhenylalanine) {}
