package com.keldorn.phenylalaninecalculatorapi.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.FoodType;
import com.keldorn.phenylalaninecalculatorapi.domain.entity.User;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.FoodTypeRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.FoodTypeResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.PagedFoodTypeResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.params.PaginationRequest;
import com.keldorn.phenylalaninecalculatorapi.exception.ResourceNotFoundException;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;
import com.keldorn.phenylalaninecalculatorapi.repository.FoodTypeRepository;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class FoodTypeServiceTests {

    @Mock
    private FoodTypeRepository foodTypeRepository;

    @Mock
    private FoodTypeReadService foodTypeReadService;

    @Mock
    private UserService userService;

    @InjectMocks
    private FoodTypeService foodTypeService;

    private final Long foodTypeId = 1L;

    @Test
    void findById_shouldReturnFoodTypeResponse_whenFoodTypeExists() {
        FoodType foodType = TestEntityFactory.foodType();
        foodType.setId(foodTypeId);
        foodType.setUser(TestEntityFactory.user());
        foodType.getUser().setUserId(TestEntityFactory.DEFAULT_ID);
        when(foodTypeReadService.findByIdOrThrow(foodTypeId)).thenReturn(foodType);
        when(userService.getCurrentUserId()).thenReturn(TestEntityFactory.DEFAULT_ID);
        FoodTypeResponse response = foodTypeService.findById(foodTypeId);
        doAssertionsCheckOnResponse(response, foodType);
    }

    @Test
    void findById_shouldThrowException_whenResourceNotFound() {
        when(foodTypeReadService.findByIdOrThrow(foodTypeId)).thenThrow(ResourceNotFoundException.class);
        Assertions.assertThatThrownBy(() -> foodTypeService.findById(foodTypeId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAll_shouldReturnPageOfFoodTypeResponses() {
        FoodType foodType = TestEntityFactory.foodType();
        PaginationRequest paginationRequest = new PaginationRequest(0, 20);
        foodType.setId(foodTypeId);
        foodType.setUser(TestEntityFactory.user());
        foodType.getUser().setUserId(TestEntityFactory.DEFAULT_ID);
        List<FoodType> foodTypeList = List.of(foodType);
        Page<FoodType> foodTypePage = new PageImpl<>(foodTypeList);
        when(foodTypeRepository.findAll(any(Pageable.class))).thenReturn(foodTypePage);
        when(userService.getCurrentUserId()).thenReturn(TestEntityFactory.DEFAULT_ID);
        PagedFoodTypeResponse response = foodTypeService.findAll(paginationRequest);
        Assertions.assertThat(response.getContent()).hasSize(1);
        doAssertionsCheckOnResponse(response.getContent().getFirst(), foodType);
    }

    @Test
    void save_shouldReturnSavedFoodTypeResponse() {
        User user = TestEntityFactory.user();
        user.setUserId(TestEntityFactory.DEFAULT_ID);
        FoodType foodType = TestEntityFactory.foodType();
        foodType.setUser(user);
        FoodTypeRequest request = new FoodTypeRequest(foodType.getName(), foodType.getMultiplier());
        when(userService.getCurrentUserReference()).thenReturn(user);
        when(foodTypeRepository.save(foodType)).thenReturn(foodType);
        FoodTypeResponse response = foodTypeService.save(request);
        verify(foodTypeRepository).save(foodType);
        doAssertionsCheckOnResponse(response, foodType);
    }

    @Test
    void update_shouldReturnUpdatedFoodTypeResponse_whenFoodTypeExists() {
        FoodType foodType = TestEntityFactory.foodType();
        foodType.setId(foodTypeId);
        foodType.setUser(TestEntityFactory.user());
        foodType.getUser().setUserId(TestEntityFactory.DEFAULT_ID);
        FoodTypeRequest request = new FoodTypeRequest("Updated Name", 100);
        when(foodTypeReadService.findByIdOrThrow(foodTypeId)).thenReturn(foodType);
        when(userService.getCurrentUserId()).thenReturn(TestEntityFactory.DEFAULT_ID);
        when(foodTypeRepository.save(any(FoodType.class))).thenReturn(foodType);
        FoodTypeResponse response = foodTypeService.update(foodTypeId, request);
        ArgumentCaptor<FoodType> captor = ArgumentCaptor.forClass(FoodType.class);
        verify(foodTypeRepository).save(captor.capture());
        FoodType savedEntity = captor.getValue();
        Assertions.assertThat(savedEntity).isNotNull();
        Assertions.assertThat(savedEntity.getName()).isEqualTo(request.name());
        Assertions.assertThat(savedEntity.getMultiplier()).isEqualTo(request.multiplier());
        doAssertionsCheckOnResponse(response, foodType);
    }

    @Test
    void update_shouldThrowExceptionAndSaveNothing_whenResourceNotFound() {
        FoodTypeRequest request = new FoodTypeRequest("Updated Name", 100);
        when(foodTypeReadService.findByIdOrThrow(foodTypeId)).thenThrow(ResourceNotFoundException.class);
        Assertions.assertThatThrownBy(() -> foodTypeService.update(foodTypeId, request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(foodTypeRepository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteEntity() {
        FoodType foodType = TestEntityFactory.foodType();
        foodType.setUser(TestEntityFactory.user());
        foodType.getUser().setUserId(TestEntityFactory.DEFAULT_ID);
        when(foodTypeReadService.findByIdOrThrow(foodTypeId)).thenReturn(foodType);
        when(userService.getCurrentUserId()).thenReturn(TestEntityFactory.DEFAULT_ID);
        foodTypeService.deleteById(foodTypeId);
        verify(foodTypeRepository).delete(foodType);
        verify(foodTypeRepository, never()).deleteById(any());
    }

    @Test
    void delete_shouldThrowExceptionAndSaveNothing_whenResourceNotFound() {
        when(foodTypeReadService.findByIdOrThrow(foodTypeId)).thenThrow(ResourceNotFoundException.class);
        Assertions.assertThatThrownBy(() -> foodTypeService.deleteById(foodTypeId))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(foodTypeRepository, never()).delete(any());
        verify(foodTypeRepository, never()).deleteById(any());
    }

    private void doAssertionsCheckOnResponse(FoodTypeResponse response, FoodType foodType) {
        Assertions.assertThat(response.id()).isEqualTo(foodType.getId());
        Assertions.assertThat(response.name()).isEqualTo(foodType.getName());
        Assertions.assertThat(response.multiplier()).isEqualTo(foodType.getMultiplier());
    }

}
