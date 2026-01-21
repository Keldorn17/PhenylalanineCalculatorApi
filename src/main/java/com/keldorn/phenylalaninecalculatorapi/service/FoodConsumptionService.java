package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.Food;
import com.keldorn.phenylalaninecalculatorapi.domain.entity.FoodConsumption;
import com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption.FoodConsumptionRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption.FoodConsumptionResponse;
import com.keldorn.phenylalaninecalculatorapi.exception.notfound.FoodConsumptionNotFoundException;
import com.keldorn.phenylalaninecalculatorapi.mapper.FoodConsumptionMapper;
import com.keldorn.phenylalaninecalculatorapi.repository.FoodConsumptionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FoodConsumptionService {

    private final FoodConsumptionMapper foodConsumptionMapper;
    private final FoodConsumptionRepository foodConsumptionRepository;
    private final DailyIntakeService dailyIntakeService;
    private final FoodService foodService;
    private final UserService userService;

    private FoodConsumption findByIdOrThrow(Long id) {
        log.debug("Finding food consumption by id {}", id);
        return foodConsumptionRepository.findById(id)
                .orElseThrow(() -> new FoodConsumptionNotFoundException("Food consumption not found by id: " + id));
    }

    public List<FoodConsumptionResponse> findAllByDate(LocalDate date) {
        log.debug("Finding all food consumptions by date");
        ZoneId zoneId = userService.getCurrentUser().resolveZoneId();
        Instant start = date.atStartOfDay(zoneId).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(zoneId).toInstant();
        Long userId = userService.getCurrentUserId();
        return foodConsumptionRepository.findAllByUserAndConsumedAtBetween(userId, start, end)
                .stream()
                .map(foodConsumptionMapper::toResponse)
                .toList();
    }

    public FoodConsumptionResponse create(Long foodId, FoodConsumptionRequest request) {
        log.debug("Creating food consumption");
        Food food = foodService.findByIdOrThrow(foodId);
        BigDecimal phenylalanineAmount = calculatePhenylalanineAmount(food.getPhenylalanine(), request.amount());
        Instant now = Instant.now();
        LocalDate userLocalDate = LocalDate.from(now.atZone(userService.getCurrentUser().resolveZoneId()));
        dailyIntakeService.addAmount(userLocalDate, phenylalanineAmount);
        FoodConsumption foodConsumption = FoodConsumption.builder()
                .user(userService.getCurrentUser())
                .food(food)
                .consumedAt(now)
                .amount(request.amount())
                .phenylalanineAmount(phenylalanineAmount)
                .build();
        return foodConsumptionMapper.toResponse(foodConsumptionRepository.save(foodConsumption));
    }

    public FoodConsumptionResponse update(Long id, FoodConsumptionRequest request) {
        log.debug("Updating food consumption by id: {}", id);
        FoodConsumption foodConsumption = findByIdOrThrow(id);
        BigDecimal phenylalanineAmount = calculatePhenylalanineAmount(foodConsumption.getFood().getPhenylalanine(), request.amount());
        LocalDate localDate = foodConsumption.getConsumedAt().atZone(userService.getCurrentUser()
                .resolveZoneId()).toLocalDate();
        dailyIntakeService.addAmount(localDate, phenylalanineAmount.subtract(foodConsumption.getPhenylalanineAmount()));
        foodConsumption.setPhenylalanineAmount(phenylalanineAmount);
        foodConsumption.setAmount(request.amount());
        return foodConsumptionMapper.toResponse(foodConsumptionRepository.save(foodConsumption));
    }

    public void deleteById(Long id) {
        log.debug("Deleting food consumption by id: {}", id);
        FoodConsumption foodConsumption = findByIdOrThrow(id);
        LocalDate localDate = foodConsumption.getConsumedAt().atZone(userService.getCurrentUser()
                .resolveZoneId()).toLocalDate();
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
