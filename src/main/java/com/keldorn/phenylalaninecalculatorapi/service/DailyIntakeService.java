package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.dto.dailyintake.DailyIntakeResponse;
import com.keldorn.phenylalaninecalculatorapi.repository.FoodConsumptionRepository;
import com.keldorn.phenylalaninecalculatorapi.utils.TimezoneHelper;

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
    public DailyIntakeResponse findByDate(ZonedDateTime date, String timezone) {
        log.debug("Sending response for findByDate");
        Long userId = userService.getCurrentUserId();
        LocalDate localDate = date.toLocalDate();
        ZoneId zoneId = TimezoneHelper.resolveZoneId(timezone, date.getZone());
        ZonedDateTime startOfDay = localDate.atStartOfDay(zoneId);
        ZonedDateTime endOfDay = localDate.plusDays(1).atStartOfDay(zoneId);
        BigDecimal consumedPhenylalanine =
                foodConsumptionRepository.calculateDailyIntake(userId, startOfDay, endOfDay);
        BigDecimal currentUserDailyLimit = userService.getCurrentUserDailyLimit();
        return new DailyIntakeResponse(localDate, consumedPhenylalanine, currentUserDailyLimit);
    }

    @Transactional(readOnly = true)
    public DailyIntakeResponse findByDate(ZonedDateTime date) {
        return selfProvider.getObject().findByDate(date, null);
    }

    @Transactional(readOnly = true)
    public DailyIntakeResponse findByDate(LocalDate date) {
        return selfProvider.getObject().findByDate(date.atStartOfDay(ZoneOffset.UTC), null);
    }

}
