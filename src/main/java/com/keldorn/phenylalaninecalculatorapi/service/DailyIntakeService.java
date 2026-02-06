package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.DailyIntake;
import com.keldorn.phenylalaninecalculatorapi.dto.dailyintake.DailyIntakeResponse;
import com.keldorn.phenylalaninecalculatorapi.exception.conflict.DailyIntakeCannotBeLowerThanZeroException;
import com.keldorn.phenylalaninecalculatorapi.exception.notfound.DailyIntakeNotFoundException;
import com.keldorn.phenylalaninecalculatorapi.mapper.DailyIntakeMapper;
import com.keldorn.phenylalaninecalculatorapi.repository.DailyIntakeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyIntakeService {

    private final DailyIntakeRepository dailyIntakeRepository;
    private final DailyIntakeMapper dailyIntakeMapper;
    private final UserService userService;

    private DailyIntake findByDateOrThrow(LocalDate date) {
        log.debug("Getting daily intake by date");
        return dailyIntakeRepository.findByUserIdAndDate(userService.getCurrentUserId(), date)
                .orElseThrow(() -> new DailyIntakeNotFoundException("No daily intake information found at: " + date));
    }

    /**
     * Updates the total phenylalanine amount for a specific date.
     * <p>
     * This method supports both increments and decrements. Pass a positive value to add
     * to the total, or a negative value to subtract from it.
     * <p>
     * If there isn't any data registered for a specific date it will create one.
     *
     * @param date   The date for which the intake data should be updated.
     * @param amount The amount to add to (positive) or subtract from (negative) the total.
     * @throws DailyIntakeCannotBeLowerThanZeroException if the update would result in a negative total.
     */
    protected final void addAmount(LocalDate date, BigDecimal amount) {
        log.debug("Adding amount for daily intake");

        DailyIntake dailyIntake = dailyIntakeRepository
                .findByUserIdAndDate(userService.getCurrentUserId(), date)
                .orElseGet(() -> DailyIntake.builder()
                        .user(userService.getCurrentUser())
                        .date(date)
                        .totalPhenylalanine(BigDecimal.ZERO)
                        .build());

        BigDecimal updated = dailyIntake.getTotalPhenylalanine().add(amount);
        if (updated.compareTo(BigDecimal.ZERO) < 0) {
            throw new DailyIntakeCannotBeLowerThanZeroException("Daily intake cannot be lower than zero");
        }

        dailyIntake.setTotalPhenylalanine(updated);
        dailyIntakeRepository.save(dailyIntake);
    }

    public DailyIntakeResponse findByDate(LocalDate date) {
        log.debug("Sending response for findByDate");
        return dailyIntakeMapper.toResponse(findByDateOrThrow(date));
    }
}
