package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.FoodType;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.FoodTypeRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.FoodTypeResponse;
import com.keldorn.phenylalaninecalculatorapi.exception.FoodTypeNotFoundException;
import com.keldorn.phenylalaninecalculatorapi.mapper.FoodTypeMapper;
import com.keldorn.phenylalaninecalculatorapi.repository.FoodTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodTypeService {

    private final FoodTypeRepository foodTypeRepository;
    private final FoodTypeMapper foodTypeMapper;

    protected FoodType findByIdOrThrow(Long id) {
        log.debug("Getting Food Type By Id: {}", id);
        return foodTypeRepository.findById(id)
                .orElseThrow(() -> new FoodTypeNotFoundException("Food Type Not Found."));
    }

    public FoodTypeResponse findById(Long id) {
        log.debug("Finding Food Type Response By Id: {}", id);
        return foodTypeMapper.toResponse(findByIdOrThrow(id));
    }

    public List<FoodTypeResponse> findAll() {
        log.debug("Finding All Food Types");
        return foodTypeRepository.findAll()
                .stream()
                .map(foodTypeMapper::toResponse)
                .toList();
    }

    public FoodTypeResponse save(FoodTypeRequest request) {
        log.debug("Saving Food Type");
        var foodType = foodTypeRepository.save(foodTypeMapper.toEntity(request));
        return foodTypeMapper.toResponse(foodType);
    }

    public FoodTypeResponse update(Long id, FoodTypeRequest request) {
        log.debug("Updating Food Type");
        var foodType = findByIdOrThrow(id);
        foodType.setName(request.name());
        foodType.setMultiplier(request.multiplier());
        return foodTypeMapper.toResponse(foodType);
    }

    public void deleteById(Long id) {
        log.debug("Deleting Food Type By Id: {}", id);
        foodTypeRepository.delete(findByIdOrThrow(id));
    }
}
