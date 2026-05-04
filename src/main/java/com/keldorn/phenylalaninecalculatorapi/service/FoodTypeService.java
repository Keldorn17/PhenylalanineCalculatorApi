package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.FoodType;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.FoodTypeRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.FoodTypeResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.PagedFoodTypeResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.params.PaginationRequest;
import com.keldorn.phenylalaninecalculatorapi.mapper.FoodTypeMapper;
import com.keldorn.phenylalaninecalculatorapi.repository.FoodTypeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodTypeService {

    private final FoodTypeRepository foodTypeRepository;
    private final FoodTypeReadService foodTypeReadService;

    @Transactional(readOnly = true)
    public FoodTypeResponse findById(Long id) {
        log.debug("Finding Food Type Response By Id: {}", id);
        return FoodTypeMapper.INSTANCE.toModel(foodTypeReadService.findByIdOrThrow(id));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "foodTypes")
    public PagedFoodTypeResponse findAll(PaginationRequest paginationRequest) {
        log.debug("Finding All Food Types");
        Pageable pageable = PageRequest.of(paginationRequest.getPageNumber(), paginationRequest.getPageSize());
        return FoodTypeMapper.INSTANCE.toModel(foodTypeRepository.findAll(pageable));
    }

    @Transactional
    @CacheEvict(value = "foodTypes", allEntries = true)
    public FoodTypeResponse save(FoodTypeRequest request) {
        log.debug("Saving Food Type");
        var foodType = foodTypeRepository.save(FoodTypeMapper.INSTANCE.toEntity(request));
        return FoodTypeMapper.INSTANCE.toModel(foodType);
    }

    @Transactional
    @CacheEvict(value = "foodTypes", key = "#id")
    public FoodTypeResponse update(Long id, FoodTypeRequest request) {
        log.debug("Updating Food Type");
        FoodType foodType = foodTypeReadService.findByIdOrThrow(id);
        foodType.setName(request.name());
        foodType.setMultiplier(request.multiplier());
        return FoodTypeMapper.INSTANCE.toModel(foodTypeRepository.save(foodType));
    }

    @Transactional
    @CacheEvict(value = "foodTypes", key = "#id")
    public void deleteById(Long id) {
        log.debug("Deleting Food Type By Id: {}", id);
        FoodType foodType = foodTypeReadService.findByIdOrThrow(id);
        foodTypeRepository.delete(foodType);
    }

}
