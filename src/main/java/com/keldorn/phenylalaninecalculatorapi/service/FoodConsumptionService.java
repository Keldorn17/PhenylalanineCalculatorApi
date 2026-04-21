package com.keldorn.phenylalaninecalculatorapi.service;

import static com.keldorn.phenylalaninecalculatorapi.utils.TimezoneHelper.resolveZoneId;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.Food;
import com.keldorn.phenylalaninecalculatorapi.domain.entity.FoodConsumption;
import com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption.FoodConsumptionRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption.FoodConsumptionResponse;
import com.keldorn.phenylalaninecalculatorapi.exception.notfound.ResourceNotFoundException;
import com.keldorn.phenylalaninecalculatorapi.mapper.FoodConsumptionMapper;
import com.keldorn.phenylalaninecalculatorapi.repository.FoodConsumptionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodConsumptionService {

    private final FoodService foodService;
    private final UserService userService;
    private final DailyIntakeService dailyIntakeService;
    private final FoodConsumptionRepository foodConsumptionRepository;

    private final ZoneId utcZoneId = ZoneOffset.UTC;

    private FoodConsumption findByIdOrThrow(Long id, Long userId) {
        log.debug("Finding food consumption by id {}", id);
        return foodConsumptionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Food consumption not found by id: " + id));
    }

    @Transactional(readOnly = true)
    public Page<FoodConsumptionResponse> findAllByDate(LocalDate date, int page, int size, String timezone) {
        log.debug("Finding all food consumptions by date");
        ZoneId zoneId = resolveZoneId(timezone);
        Instant start = date.atStartOfDay(zoneId).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(zoneId).toInstant();
        Long userId = userService.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        return foodConsumptionRepository.findAllByUserAndConsumedAtBetween(userId, start, end, pageable)
                .map(foodConsumption -> FoodConsumptionMapper.INSTANCE.toResponse(foodConsumption, zoneId));
    }

    @Transactional
    public FoodConsumptionResponse save(Long foodId, FoodConsumptionRequest request, String timezone) {
        log.debug("Creating food consumption");
        Food food = foodService.findByIdOrThrow(foodId);
        BigDecimal phenylalanineAmount = calculatePhenylalanineAmount(food.getPhenylalanine(), request.amount());
        Instant now = Instant.now();
        ZoneId userZoneId = resolveZoneId(timezone);
        LocalDate userLocalDate = LocalDate.ofInstant(now, userZoneId);
        dailyIntakeService.addAmount(userLocalDate, phenylalanineAmount);
        FoodConsumption foodConsumption = FoodConsumption.builder()
                .user(userService.getCurrentUser())
                .food(food)
                .consumedAt(now)
                .amount(request.amount())
                .phenylalanineAmount(phenylalanineAmount)
                .build();
        return FoodConsumptionMapper.INSTANCE.toResponse(foodConsumptionRepository.save(foodConsumption), utcZoneId);
    }

    @Transactional
    public FoodConsumptionResponse update(Long id, FoodConsumptionRequest request, String timezone) {
        log.debug("Updating food consumption by id: {}", id);
        FoodConsumption foodConsumption = findByIdOrThrow(id, userService.getCurrentUserId());
        BigDecimal phenylalanineAmount =
                calculatePhenylalanineAmount(foodConsumption.getFood().getPhenylalanine(), request.amount());
        ZoneId userZoneId = resolveZoneId(timezone);
        LocalDate localDate = LocalDate.ofInstant(foodConsumption.getConsumedAt(), userZoneId);
        dailyIntakeService.addAmount(localDate, phenylalanineAmount.subtract(foodConsumption.getPhenylalanineAmount()));
        foodConsumption.setPhenylalanineAmount(phenylalanineAmount);
        foodConsumption.setAmount(request.amount());
        return FoodConsumptionMapper.INSTANCE.toResponse(foodConsumptionRepository.save(foodConsumption), utcZoneId);
    }

    @Transactional
    public void deleteById(Long id, String timezone) {
        log.debug("Deleting food consumption by id: {}", id);
        FoodConsumption foodConsumption = findByIdOrThrow(id, userService.getCurrentUserId());
        ZoneId userZoneId = resolveZoneId(timezone);
        LocalDate localDate = LocalDate.ofInstant(foodConsumption.getConsumedAt(), userZoneId);
        dailyIntakeService.addAmount(localDate, foodConsumption.getPhenylalanineAmount().negate());
        foodConsumptionRepository.delete(foodConsumption);
    }

    private BigDecimal calculatePhenylalanineAmount(BigDecimal phenylalanine, BigDecimal amount) {
        log.debug("Calculating phenylalanine amount");
        return phenylalanine.multiply(amount)
                .divide(BigDecimal.valueOf(1000), RoundingMode.HALF_UP)
                .setScale(4, RoundingMode.HALF_UP);
    }

}
