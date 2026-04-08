package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.repository.DailyIntakeRepository;
import com.keldorn.phenylalaninecalculatorapi.repository.FoodConsumptionRepository;
import com.keldorn.phenylalaninecalculatorapi.repository.FoodRepository;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteUserAssociationsService {

    private final FoodRepository foodRepository;
    private final DailyIntakeRepository dailyIntakeRepository;
    private final FoodConsumptionRepository foodConsumptionRepository;

    @Transactional
    public void removeAssociation(Long userId) {
        foodRepository.updateFoodUser(userId, null);
        dailyIntakeRepository.deleteDailyIntakeByUserId(userId);
        foodConsumptionRepository.deleteFoodConsumptionByUserId(userId);
    }
}
