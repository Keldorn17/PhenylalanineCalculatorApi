package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.FoodType;
import com.keldorn.phenylalaninecalculatorapi.exception.ResourceNotFoundException;
import com.keldorn.phenylalaninecalculatorapi.repository.FoodTypeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodTypeReadService {

    private final FoodTypeRepository foodTypeRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "foodTypes", key = "#id")
    public FoodType findByIdOrThrow(Long id) {
        log.debug("Getting Food Type By Id: {}", id);
        return foodTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food Type Not Found."));
    }

}
