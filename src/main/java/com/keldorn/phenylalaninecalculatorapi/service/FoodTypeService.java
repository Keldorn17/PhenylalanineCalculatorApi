package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.FoodType;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.FoodTypeRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.FoodTypeResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.params.PaginationRequest;
import com.keldorn.phenylalaninecalculatorapi.exception.ResourceNotFoundException;
import com.keldorn.phenylalaninecalculatorapi.mapper.FoodTypeMapper;
import com.keldorn.phenylalaninecalculatorapi.repository.FoodTypeRepository;

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
public class FoodTypeService {

    private final FoodTypeRepository foodTypeRepository;

    protected FoodType findByIdOrThrow(Long id) {
        log.debug("Getting Food Type By Id: {}", id);
        return foodTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food Type Not Found."));
    }

    @Transactional(readOnly = true)
    public FoodTypeResponse findById(Long id) {
        log.debug("Finding Food Type Response By Id: {}", id);
        return FoodTypeMapper.INSTANCE.toResponse(findByIdOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<FoodTypeResponse> findAll(PaginationRequest paginationRequest) {
        log.debug("Finding All Food Types");
        Pageable pageable = PageRequest.of(paginationRequest.getPageNumber(), paginationRequest.getPageSize());
        return foodTypeRepository.findAll(pageable)
                .map(FoodTypeMapper.INSTANCE::toResponse);
    }

    @Transactional
    public FoodTypeResponse save(FoodTypeRequest request) {
        log.debug("Saving Food Type");
        var foodType = foodTypeRepository.save(FoodTypeMapper.INSTANCE.toEntity(request));
        return FoodTypeMapper.INSTANCE.toResponse(foodType);
    }

    @Transactional
    public FoodTypeResponse update(Long id, FoodTypeRequest request) {
        log.debug("Updating Food Type");
        var foodType = findByIdOrThrow(id);
        foodType.setName(request.name());
        foodType.setMultiplier(request.multiplier());
        return FoodTypeMapper.INSTANCE.toResponse(foodTypeRepository.save(foodType));
    }

    @Transactional
    public void deleteById(Long id) {
        log.debug("Deleting Food Type By Id: {}", id);
        foodTypeRepository.delete(findByIdOrThrow(id));
    }

}
