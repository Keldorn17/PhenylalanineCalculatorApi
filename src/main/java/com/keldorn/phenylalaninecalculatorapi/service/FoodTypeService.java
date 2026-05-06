package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.constant.ApiResponses;
import com.keldorn.phenylalaninecalculatorapi.domain.entity.FoodType;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.FoodTypeRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.FoodTypeResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.PagedFoodTypeResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.params.PaginationRequest;
import com.keldorn.phenylalaninecalculatorapi.exception.CannotEditResourceException;
import com.keldorn.phenylalaninecalculatorapi.mapper.FoodTypeMapper;
import com.keldorn.phenylalaninecalculatorapi.repository.FoodTypeRepository;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

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

    private final UserService userService;
    private final FoodTypeRepository foodTypeRepository;
    private final FoodTypeReadService foodTypeReadService;

    @Transactional(readOnly = true)
    public FoodTypeResponse findById(Long id) {
        log.debug("Finding Food Type Response By Id: {}", id);
        return FoodTypeMapper.INSTANCE.toModel(foodTypeReadService.findByIdOrThrow(id), userService.getCurrentUserId());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "foodTypePages", key = "{#paginationRequest, @userService.getCurrentUserId()}")
    public PagedFoodTypeResponse findAll(PaginationRequest paginationRequest) {
        log.debug("Finding All Food Types");
        Pageable pageable = PageRequest.of(paginationRequest.getPageNumber(), paginationRequest.getPageSize());
        return FoodTypeMapper.INSTANCE.toModel(foodTypeRepository.findAll(pageable), userService.getCurrentUserId());
    }

    @Transactional
    @CacheEvict(value = "foodTypePages", allEntries = true)
    public FoodTypeResponse save(FoodTypeRequest request) {
        log.debug("Saving Food Type");
        FoodType entity = FoodTypeMapper.INSTANCE.toEntity(request);
        entity.setUser(userService.getCurrentUserReference());
        var foodType = foodTypeRepository.save(entity);
        return FoodTypeMapper.INSTANCE.toModel(foodType, userService.getCurrentUserId());
    }

    @Transactional
    @CacheEvict(value = {"foodTypeEntities", "foodTypePages", "foodEntities", "foodPages"}, allEntries = true)
    public FoodTypeResponse update(Long id, FoodTypeRequest request) {
        log.debug("Updating Food Type");
        FoodType foodType = foodTypeReadService.findByIdOrThrow(id);
        foodType.setName(request.name());
        foodType.setMultiplier(request.multiplier());
        Long currentUserId = userService.getCurrentUserId();
        canEditOrThrow(currentUserId, foodType.getUser().getUserId());
        return FoodTypeMapper.INSTANCE.toModel(foodTypeRepository.save(foodType), currentUserId);
    }

    @Transactional
    @CacheEvict(value = {"foodTypeEntities", "foodTypePages", "foodEntities", "foodPages"}, allEntries = true)
    public void deleteById(Long id) {
        log.debug("Deleting Food Type By Id: {}", id);
        FoodType foodType = foodTypeReadService.findByIdOrThrow(id);
        Long currentUserId = userService.getCurrentUserId();
        canEditOrThrow(currentUserId, foodType.getUser().getUserId());
        foodTypeRepository.delete(foodType);
    }

    private void canEditOrThrow(@NotNull Long currentUserId, @Nullable Long resourceUserId) {
        if (!currentUserId.equals(resourceUserId)) {
            throw new CannotEditResourceException(ApiResponses.UNOWNED_RESOURCE_RESPONSE);
        }
    }

}
