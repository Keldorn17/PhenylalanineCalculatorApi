package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.Food;
import com.keldorn.phenylalaninecalculatorapi.domain.entity.FoodType;
import com.keldorn.phenylalaninecalculatorapi.domain.entity.User;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodUpdateRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.food.PagedFoodResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.params.PaginationRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.params.QueryRequest;
import com.keldorn.phenylalaninecalculatorapi.exception.InvalidRSQLException;
import com.keldorn.phenylalaninecalculatorapi.exception.ResourceNotFoundException;
import com.keldorn.phenylalaninecalculatorapi.mapper.FoodMapper;
import com.keldorn.phenylalaninecalculatorapi.repository.FoodRepository;
import com.keldorn.phenylalaninecalculatorapi.utils.FoodQueryParamsUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.core.PropertyReferenceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.jirutka.rsql.parser.RSQLParserException;
import io.github.perplexhub.rsql.UnknownPropertyException;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodService {

    private final UserService userService;
    private final FoodRepository foodRepository;
    private final FoodTypeService foodTypeService;

    protected Food findByIdOrThrow(Long id) {
        log.debug("Getting Food By Id: {}", id);
        return foodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food Not Found."));
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

    @Transactional(readOnly = true)
    public FoodResponse findById(Long id) {
        log.debug("Finding Food By Id: {}", id);
        return FoodMapper.INSTANCE.toModel(findByIdOrThrow(id));
    }

    @Transactional
    public PagedFoodResponse findAll(QueryRequest queryRequest, PaginationRequest paginationRequest) {
        log.debug("Finding All Foods");
        PageRequest pageRequest = PageRequest.of(paginationRequest.getPageNumber(), paginationRequest.getPageSize());
        Page<Long> foodIdResponse = fetchPaginatedFoodIds(queryRequest, pageRequest);
        if (foodIdResponse.isEmpty()) {
            return FoodMapper.INSTANCE.toModel(Page.empty(pageRequest));
        }
        Page<Food> loadedPage = loadAndSortFood(foodIdResponse, pageRequest);
        return FoodMapper.INSTANCE.toModel(loadedPage);
    }

    @Transactional
    public FoodResponse save(FoodRequest request) {
        log.debug("Saving Food");
        Food food = FoodMapper.INSTANCE.toEntity(request);
        addTypeToFood(food, request);
        addUserToFood(food);
        updatePhenylalanine(food);
        return FoodMapper.INSTANCE.toModel(foodRepository.save(food));
    }

    @Transactional
    public FoodResponse update(Long id, FoodUpdateRequest request) {
        log.debug("Updating Food By Id: {}", id);
        Food food = findByIdOrThrow(id);
        FoodMapper.INSTANCE.updateEntity(request, food);
        if (request.foodTypeId() != null) {
            FoodType foodType = foodTypeService.findByIdOrThrow(request.foodTypeId());
            food.setFoodType(foodType);
        }
        updatePhenylalanine(food);
        return FoodMapper.INSTANCE.toModel(foodRepository.save(food));
    }

    @Transactional
    public void deleteById(Long id) {
        log.debug("Deleting Food By Id: {}", id);
        foodRepository.delete(findByIdOrThrow(id));
    }

    private Page<Long> fetchPaginatedFoodIds(QueryRequest request, PageRequest pageRequest) {
        try {
            Specification<Food> querySpecification = FoodQueryParamsUtil.createQuerySpecification(request);
            return foodRepository.findSortedFoodIds(querySpecification, pageRequest);
        } catch (RSQLParserException | IllegalArgumentException | PropertyReferenceException |
                 UnknownPropertyException ex) {
            log.debug("Invalid query or sort parameters provided query='{}', sort='{}'",
                    request.getQuery(), request.getSort());
            throw new InvalidRSQLException("Invalid query or sort parameters");
        }
    }

    private Page<Food> loadAndSortFood(Page<Long> pagedIdResult, PageRequest pageRequest) {
        List<Long> foodIds = pagedIdResult.getContent();
        List<Food> loadedFoods = foodRepository.findAllByIds(foodIds);
        Map<Long, Food> foodMap = loadedFoods.stream()
                .collect(Collectors.toMap(Food::getId, Function.identity()));
        List<Food> sortedFoods = foodIds.stream()
                .map(foodMap::get)
                .toList();
        return new PageImpl<>(sortedFoods, pageRequest, pagedIdResult.getTotalElements());
    }

}
