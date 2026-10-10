package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.dto.dailyintake.DailyIntakeResponse;
import com.keldorn.phenylalaninecalculatorapi.repository.FoodConsumptionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyIntakeService {

    private final UserService userService;
    private final FoodConsumptionRepository foodConsumptionRepository;
    private final ObjectProvider<DailyIntakeService> selfProvider;

    @Transactional(readOnly = true)
    public DailyIntakeResponse findByDate(ZonedDateTime date) {
        log.debug("Sending response for findByDate");
        Long userId = userService.getCurrentUserId();
        LocalDate localDate = date.toLocalDate();
        ZoneId zoneId = date.getZone();
        ZonedDateTime startOfDay = localDate.atStartOfDay(zoneId);
        ZonedDateTime endOfDay = startOfDay.plusDays(1);
        BigDecimal totalPhenylalanine =
                foodConsumptionRepository.calculateDailyIntake(userId, startOfDay, endOfDay);
        return new DailyIntakeResponse(localDate, totalPhenylalanine);
    }

    @Transactional(readOnly = true)
    public DailyIntakeResponse findByDate(LocalDate date) {
        return selfProvider.getObject().findByDate(date.atStartOfDay(ZoneOffset.UTC));
    }

}
