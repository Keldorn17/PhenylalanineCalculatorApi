package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.Food;
import com.keldorn.phenylalaninecalculatorapi.domain.entity.FoodConsumption;
import com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption.FoodConsumptionCreateRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption.FoodConsumptionRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption.FoodConsumptionResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption.PagedFoodConsumptionResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.params.PaginationRequest;
import com.keldorn.phenylalaninecalculatorapi.exception.ResourceNotFoundException;
import com.keldorn.phenylalaninecalculatorapi.mapper.FoodConsumptionMapper;
import com.keldorn.phenylalaninecalculatorapi.repository.FoodConsumptionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodConsumptionService {

    private final UserService userService;
    private final FoodReadService foodReadService;
    private final FoodConsumptionRepository foodConsumptionRepository;
    private final ObjectProvider<FoodConsumptionService> selfProvider;

    private FoodConsumption findByIdOrThrow(Long id, Long userId) {
        log.debug("Finding food consumption by id {}", id);
        return foodConsumptionRepository.findByIdAndUser_UserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Food consumption not found by id: " + id));
    }

    @Transactional(readOnly = true)
    public PagedFoodConsumptionResponse findAllByDate(ZonedDateTime date, PaginationRequest paginationRequest) {
        log.debug("Finding all food consumptions by date");
        LocalDate localDate = date.toLocalDate();
        ZoneId zoneId = date.getZone();
        ZonedDateTime start = localDate.atStartOfDay(zoneId);
        ZonedDateTime end = start.plusDays(1);
        Long userId = userService.getCurrentUserId();
        Pageable pageable = PageRequest.of(paginationRequest.getPageNumber(), paginationRequest.getPageSize());
        Page<FoodConsumption> response =
                foodConsumptionRepository.findAllByUserAndConsumedAtBetween(userId, start, end, pageable);
        return FoodConsumptionMapper.INSTANCE.toModel(response);
    }

    @Transactional(readOnly = true)
    public PagedFoodConsumptionResponse findAllByDate(LocalDate date, PaginationRequest paginationRequest) {
        return selfProvider.getObject().findAllByDate(date.atStartOfDay(ZoneOffset.UTC), paginationRequest);
    }

    @Transactional
    public FoodConsumptionResponse save(FoodConsumptionCreateRequest request) {
        log.debug("Creating food consumption");
        Food food = foodReadService.findByIdOrThrow(request.foodId());
        BigDecimal phenylalanineAmount = calculatePhenylalanineAmount(food.getPhenylalanine(), request.amount());
        ZonedDateTime consumedAt = request.consumedAt() != null ? request.consumedAt() : ZonedDateTime.now();
        FoodConsumption foodConsumption = FoodConsumption.builder()
                .user(userService.getCurrentUserReference())
                .food(food)
                .consumedAt(consumedAt)
                .amount(request.amount())
                .phenylalanineAmount(phenylalanineAmount)
                .build();
        return FoodConsumptionMapper.INSTANCE.toModel(foodConsumptionRepository.save(foodConsumption));
    }

    @Transactional
    public FoodConsumptionResponse update(Long id, FoodConsumptionRequest request) {
        log.debug("Updating food consumption by id: {}", id);
        FoodConsumption foodConsumption = findByIdOrThrow(id, userService.getCurrentUserId());
        BigDecimal phenylalanineAmount =
                calculatePhenylalanineAmount(foodConsumption.getFood().getPhenylalanine(), request.amount());
        if (request.consumedAt() != null) {
            foodConsumption.setConsumedAt(request.consumedAt());
        }
        foodConsumption.setPhenylalanineAmount(phenylalanineAmount);
        foodConsumption.setAmount(request.amount());
        return FoodConsumptionMapper.INSTANCE.toModel(foodConsumptionRepository.save(foodConsumption));
    }

    @Transactional
    public void deleteById(Long id) {
        log.debug("Deleting food consumption by id: {}", id);
        FoodConsumption foodConsumption = findByIdOrThrow(id, userService.getCurrentUserId());
        foodConsumptionRepository.delete(foodConsumption);
    }

    private BigDecimal calculatePhenylalanineAmount(BigDecimal phenylalanine, BigDecimal amount) {
        log.debug("Calculating phenylalanine amount");
        return phenylalanine.multiply(amount)
                .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP)
                .setScale(4, RoundingMode.HALF_UP);
    }

}
