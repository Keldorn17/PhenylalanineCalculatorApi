package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.Food;
import com.keldorn.phenylalaninecalculatorapi.exception.ResourceNotFoundException;
import com.keldorn.phenylalaninecalculatorapi.repository.FoodRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodReadService {

    private final FoodRepository foodRepository;

    @Transactional(readOnly = true)
    public Food findByIdOrThrow(Long id, Long userId) {
        log.debug("Getting Food By Id: {} for User Id: {}", id, userId);
        return foodRepository.findByIdAndVisibleToUser(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Food Not Found."));
    }

}
