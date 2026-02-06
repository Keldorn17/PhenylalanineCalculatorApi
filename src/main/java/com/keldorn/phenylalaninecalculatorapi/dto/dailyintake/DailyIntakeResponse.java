package com.keldorn.phenylalaninecalculatorapi.dto.dailyintake;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyIntakeResponse(Long id, LocalDate date, BigDecimal totalPhenylalanine) {}
