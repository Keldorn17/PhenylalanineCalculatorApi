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
import com.keldorn.phenylalaninecalculatorapi.mapper.FoodMapper;
import com.keldorn.phenylalaninecalculatorapi.repository.FoodRepository;
import com.keldorn.phenylalaninecalculatorapi.utils.FoodQueryParamsUtil;

import java.math.BigDecimal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.data.domain.Page;
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
    private final FoodReadService foodReadService;
    private final FoodTypeReadService foodTypeReadService;

    @Transactional(readOnly = true)
    public FoodResponse findById(Long id) {
        log.debug("Finding Food By Id: {}", id);
        return FoodMapper.INSTANCE.toModel(foodReadService.findByIdOrThrow(id));
    }

    @Transactional(readOnly = true)
    public PagedFoodResponse findAll(QueryRequest queryRequest, PaginationRequest paginationRequest) {
        log.debug("Finding All Foods");
        PageRequest pageRequest = PageRequest.of(paginationRequest.getPageNumber(), paginationRequest.getPageSize());
        try {
            Specification<Food> querySpecification = FoodQueryParamsUtil.createQuerySpecification(queryRequest);
            Page<Food> response = foodRepository.findAll(querySpecification, pageRequest);
            return FoodMapper.INSTANCE.toModel(response);
        } catch (RSQLParserException | IllegalArgumentException | PropertyReferenceException |
                 UnknownPropertyException ex) {
            log.debug("Invalid query or sort parameters provided query='{}', sort='{}'",
                    queryRequest.getQuery(), queryRequest.getSort());
            throw new InvalidRSQLException("Invalid query or sort parameters");
        }
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
    @CacheEvict(value = "foods", key = "#id")
    public FoodResponse update(Long id, FoodUpdateRequest request) {
        log.debug("Updating Food By Id: {}", id);
        Food food = foodReadService.findByIdOrThrow(id);
        FoodMapper.INSTANCE.updateEntity(request, food);
        if (request.foodTypeId() != null) {
            FoodType foodType = foodTypeReadService.findByIdOrThrow(request.foodTypeId());
            food.setFoodType(foodType);
        }
        updatePhenylalanine(food);
        return FoodMapper.INSTANCE.toModel(foodRepository.save(food));
    }

    @Transactional
    @CacheEvict(value = "foods", key = "#id")
    public void deleteById(Long id) {
        log.debug("Deleting Food By Id: {}", id);
        Food food = foodReadService.findByIdOrThrow(id);
        foodRepository.delete(food);
    }

    private void addTypeToFood(Food food, FoodRequest request) {
        log.debug("Adding Food Type To Food. Food Type Id: {}", request.foodTypeId());
        FoodType foodType = foodTypeReadService.findByIdOrThrow(request.foodTypeId());
        food.setFoodType(foodType);
    }

    private void addUserToFood(Food food) {
        log.debug("Adding Current User to Food");
        User user = userService.getCurrentUserReference();
        food.setUser(user);
    }

    private void updatePhenylalanine(Food food) {
        if (food.getFoodType().getMultiplier() != null) {
            BigDecimal updated = food.getProtein()
                    .multiply(BigDecimal.valueOf(food.getFoodType().getMultiplier()));
            food.setPhenylalanine(updated);
        }
    }

}
