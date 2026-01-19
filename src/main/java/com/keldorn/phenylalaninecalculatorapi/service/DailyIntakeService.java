package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.DailyIntake;
import com.keldorn.phenylalaninecalculatorapi.dto.dailyintake.DailyIntakeResponse;
import com.keldorn.phenylalaninecalculatorapi.exception.DailyIntakeCannotBeLowerThanZeroException;
import com.keldorn.phenylalaninecalculatorapi.exception.DailyIntakeNotFoundException;
import com.keldorn.phenylalaninecalculatorapi.mapper.DailyIntakeMapper;
import com.keldorn.phenylalaninecalculatorapi.repository.DailyIntakeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DailyIntakeService {

    private final DailyIntakeRepository dailyIntakeRepository;
    private final DailyIntakeMapper dailyIntakeMapper;
    private final UserService userService;

    private DailyIntake findByDateToThrow(LocalDate date) {
        return dailyIntakeRepository.findByUserIdAndDate(userService.getCurrentUserId(), date)
                .orElseThrow(() -> new DailyIntakeNotFoundException("No daily intake information found at: " + date));
    }

    /**
     * Updates the total phenylalanine amount for a specific date.
     * <p>
     * This method supports both increments and decrements. Pass a positive value to add
     * to the total, or a negative value to subtract from it.
     *
     * @param date   The date for which the intake data should be updated.
     * @param amount The amount to add to (positive) or subtract from (negative) the total.
     * @return The updated {@link DailyIntake} entity.
     * @throws DailyIntakeNotFoundException if no record exists for the given date.
     * @throws DailyIntakeCannotBeLowerThanZeroException if the update would result in a negative total.
     */
    protected final DailyIntake update(LocalDate date, BigDecimal amount) {
        DailyIntake dailyIntake = findByDateToThrow(date);
        dailyIntake.setTotalPhenylalanine(dailyIntake.getTotalPhenylalanine().add(amount));
        if (dailyIntake.getTotalPhenylalanine().compareTo(BigDecimal.ZERO) < 0) {
            throw new DailyIntakeCannotBeLowerThanZeroException("Daily intake cannot be lower than zero");
        }
        dailyIntakeRepository.save(dailyIntake);
        return dailyIntake;
    }

    public DailyIntakeResponse findByDate(LocalDate date) {
        return dailyIntakeMapper.toResponse(findByDateToThrow(date));
    }
}
