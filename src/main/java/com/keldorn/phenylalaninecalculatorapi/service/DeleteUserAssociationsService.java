package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.repository.DailyIntakeRepository;
import com.keldorn.phenylalaninecalculatorapi.repository.FoodConsumptionRepository;
import com.keldorn.phenylalaninecalculatorapi.repository.FoodRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteUserAssociationsService {

    private final FoodRepository foodRepository;
    private final DailyIntakeRepository dailyIntakeRepository;
    private final FoodConsumptionRepository foodConsumptionRepository;

    @Transactional
    public void removeAssociation(Long userId) {
        int foodCount = foodRepository.updateFoodUser(userId, null);
        log.debug("Removed food associations {}, for user: {}", foodCount, userId);
        int dailyIntakeCount = dailyIntakeRepository.deleteDailyIntakeByUserId(userId);
        log.debug("Deleted daily intake {}, for user {}", dailyIntakeCount, userId);
        int foodConsumptionCount = foodConsumptionRepository.deleteFoodConsumptionByUserId(userId);
        log.debug("Deleted food consumption {}, for user {}", foodConsumptionCount, userId);

    }

}
