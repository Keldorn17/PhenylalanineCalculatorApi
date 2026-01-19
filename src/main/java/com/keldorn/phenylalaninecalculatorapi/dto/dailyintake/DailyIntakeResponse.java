package com.keldorn.phenylalaninecalculatorapi.dto.dailyintake;

import java.math.BigDecimal;
import java.util.Date;

public record DailyIntakeResponse(Long id, Date date, BigDecimal totalPhenylalanine) {}
