package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.FoodType;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.FoodTypeRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.FoodTypeResponse;
import com.keldorn.phenylalaninecalculatorapi.exception.FoodTypeNotFoundException;
import com.keldorn.phenylalaninecalculatorapi.mapper.FoodTypeMapper;
import com.keldorn.phenylalaninecalculatorapi.repository.FoodTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodTypeService {

    private final FoodTypeRepository foodTypeRepository;
    private final FoodTypeMapper foodTypeMapper;

    private FoodType findByIdOrThrow(Long id) {
        return foodTypeRepository.findById(id)
                .orElseThrow(() -> new FoodTypeNotFoundException("Food Type Not Found."));
    }

    public FoodTypeResponse findById(Long id) {
        return foodTypeMapper.toResponse(findByIdOrThrow(id));
    }

    public List<FoodTypeResponse> findAll() {
        return foodTypeRepository.findAll()
                .stream()
                .map(foodTypeMapper::toResponse)
                .toList();
    }

    public FoodTypeResponse save(FoodTypeRequest request) {
        var foodType = foodTypeRepository.save(foodTypeMapper.toEntity(request));
        return foodTypeMapper.toResponse(foodType);
    }

    public FoodTypeResponse update(Long id, FoodTypeRequest request) {
        var foodType = findByIdOrThrow(id);
        foodType.setName(request.name());
        foodType.setMultiplier(request.multiplier());
        return foodTypeMapper.toResponse(foodType);
    }

    public void deleteById(Long id) {
        findByIdOrThrow(id);
        foodTypeRepository.deleteById(id);
    }
}
