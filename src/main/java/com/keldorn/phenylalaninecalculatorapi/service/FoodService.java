package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.Food;
import com.keldorn.phenylalaninecalculatorapi.domain.entity.FoodType;
import com.keldorn.phenylalaninecalculatorapi.domain.entity.User;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodUpdateRequest;
import com.keldorn.phenylalaninecalculatorapi.exception.notfound.FoodNotFoundException;
import com.keldorn.phenylalaninecalculatorapi.mapper.FoodMapper;
import com.keldorn.phenylalaninecalculatorapi.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodService {

    private final FoodRepository foodRepository;
    private final FoodMapper foodMapper;
    private final UserService userService;
    private final FoodTypeService foodTypeService;

    protected Food findByIdOrThrow(Long id) {
        log.debug("Getting Food By Id: {}", id);
        return foodRepository.findById(id)
                .orElseThrow(() -> new FoodNotFoundException("Food Not Found."));
    }

    private void addTypeToFood(Food food, FoodRequest request) {
        log.debug("Adding Food Type To Food. Food Type Id: {}", request.foodTypeId());
        FoodType foodType = foodTypeService.findByIdOrThrow(request.foodTypeId());
        food.setFoodType(foodType);
    }

    private void addUserToFood(Food food) {
        log.debug("Adding Current User to Food");
        User user = userService.getCurrentUser();
        food.setUser(user);
    }

    private void updatePhenylalanine(Food food) {
        if (food.getFoodType().getMultiplier() != null) {
            BigDecimal updated = food.getProtein()
                    .multiply(BigDecimal.valueOf(food.getFoodType().getMultiplier()));
            food.setPhenylalanine(updated);
        }
    }

    public FoodResponse findById(Long id) {
        log.debug("Finding Food By Id: {}", id);
        return foodMapper.toResponse(findByIdOrThrow(id));
    }

    public Page<FoodResponse> findAll(int page, int size) {
        log.debug("Finding All Foods");
        Pageable pageable = PageRequest.of(page, size);
        return foodRepository.findAll(pageable)
                .map(foodMapper::toResponse);
    }

    public FoodResponse save(FoodRequest request) {
        log.debug("Saving Food");
        Food food = foodMapper.toEntity(request);
        addTypeToFood(food, request);
        addUserToFood(food);
        updatePhenylalanine(food);
        return foodMapper.toResponse(foodRepository.save(food));
    }

    public FoodResponse update(Long id, FoodUpdateRequest request) {
        log.debug("Updating Food By Id: {}", id);
        Food food = findByIdOrThrow(id);
        if (request.name() != null) food.setName(request.name());
        if (request.protein() != null) food.setProtein(request.protein());
        if (request.calories() != null) food.setCalories(request.calories());
        if (request.foodTypeId() != null) {
            FoodType foodType = foodTypeService.findByIdOrThrow(request.foodTypeId());
            food.setFoodType(foodType);
        }
        updatePhenylalanine(food);
        return foodMapper.toResponse(foodRepository.save(food));
    }

    public void deleteById(Long id) {
        log.debug("Deleting Food By Id: {}", id);
        foodRepository.delete(findByIdOrThrow(id));
    }
}
