package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.Food;
import com.keldorn.phenylalaninecalculatorapi.exception.ResourceNotFoundException;
import com.keldorn.phenylalaninecalculatorapi.repository.FoodRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodReadService {

    private final FoodRepository foodRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "foods", key = "#id")
    public Food findByIdOrThrow(Long id) {
        log.debug("Getting Food By Id: {}", id);
        return foodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food Not Found."));
    }

}
