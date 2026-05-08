package com.keldorn.phenylalaninecalculatorapi.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.Food;
import com.keldorn.phenylalaninecalculatorapi.domain.entity.FoodType;
import com.keldorn.phenylalaninecalculatorapi.domain.entity.User;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodUpdateRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.food.PagedFoodResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.params.PaginationRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.params.QueryRequest;
import com.keldorn.phenylalaninecalculatorapi.exception.ResourceNotFoundException;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;
import com.keldorn.phenylalaninecalculatorapi.repository.FoodRepository;

import java.math.BigDecimal;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class FoodServiceTests {

    @Mock
    private FoodRepository foodRepository;

    @Mock
    private FoodTypeService foodTypeService;

    @Mock
    private FoodTypeReadService foodTypeReadService;

    @Mock
    private FoodReadService foodReadService;

    @Mock
    private UserService userService;

    @InjectMocks
    private FoodService foodService;

    private final Long foodId = 1L;

    @Test
    void findById_shouldThrowFoodNotFoundException_whenResourceNotFound() {
        when(foodReadService.findByIdOrThrow(foodId)).thenThrow(ResourceNotFoundException.class);
        Assertions.assertThatThrownBy(() -> foodService.findById(foodId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findById_shouldReturnFoodResponse_whenFoodExists() {
        Food food = TestEntityFactory.food(TestEntityFactory.foodType());
        when(foodReadService.findByIdOrThrow(foodId)).thenReturn(food);
        FoodResponse response = foodService.findById(foodId);
        doAssertionsCheckOnResponse(response, food);
    }

    @Test
    void findAll_shouldReturnPageOfFoodResponses() {
        Food food = TestEntityFactory.food(TestEntityFactory.foodType());
        food.setId(1L);
        when(foodRepository.findAll(ArgumentMatchers.<Specification<Food>>any(), any(Pageable.class))).thenReturn(
                new PageImpl<>(List.of(food)));
        PagedFoodResponse response = foodService.findAll(new QueryRequest(), new PaginationRequest(0, 20));
        Assertions.assertThat(response.getContent()).hasSize(1);
        doAssertionsCheckOnResponse(response.getContent().getFirst(), food);
    }

    @Test
    void findAll_shouldReturnEmptyList() {
        when(foodRepository.findAll(ArgumentMatchers.<Specification<Food>>any(), any(Pageable.class))).thenReturn(Page.empty());
        PagedFoodResponse response = foodService.findAll(new QueryRequest(), new PaginationRequest(0, 20));
        Assertions.assertThat(response.getContent()).isEmpty();
    }

    @Test
    void save_shouldReturnsFoodResponse() {
        Long foodTypeId = 1L;
        FoodRequest request = new FoodRequest("Test Name", BigDecimal.TEN, BigDecimal.TEN, 1L);
        FoodType foodType = TestEntityFactory.foodType();
        foodType.setMultiplier(1);
        foodType.setName("Updated Type Name");
        Food food = TestEntityFactory.food(foodType);
        food.setProtein(BigDecimal.TEN);
        food.setPhenylalanine(BigDecimal.TEN);
        when(foodTypeReadService.findByIdOrThrow(foodTypeId)).thenReturn(foodType);
        when(userService.getCurrentUserReference()).thenReturn(TestEntityFactory.user());
        when(foodRepository.save(any(Food.class))).thenReturn(food);
        FoodResponse response = foodService.save(request);
        ArgumentCaptor<Food> captor = ArgumentCaptor.forClass(Food.class);
        verify(foodRepository).save(captor.capture());
        Food savedFood = captor.getValue();
        Assertions.assertThat(savedFood.getUser()).isNotNull();
        Assertions.assertThat(savedFood.getFoodType()).isNotNull();
        Assertions.assertThat(savedFood.getPhenylalanine())
                .isEqualByComparingTo(BigDecimal.TEN);
        Assertions.assertThat(savedFood.getFoodType().getName()).isEqualTo(foodType.getName());
        doAssertionsCheckOnResponse(response, food);
    }

    @Test
    void save_shouldThrowExceptionAndSaveNothing_whenFoodTypeNotFound() {
        FoodRequest request = new FoodRequest(null, null, null, 1L);
        when(foodTypeReadService.findByIdOrThrow(request.foodTypeId()))
                .thenThrow(ResourceNotFoundException.class);
        Assertions.assertThatThrownBy(() -> foodService.save(request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(foodRepository, never()).save(any());
    }

    @Test
    void save_shouldThrowExceptionAndSaveNothing_whenUserNotFound() {
        FoodRequest request = new FoodRequest("Test", BigDecimal.TEN, BigDecimal.TEN, 1L);
        when(foodTypeReadService.findByIdOrThrow(request.foodTypeId()))
                .thenReturn(TestEntityFactory.foodType());
        when(userService.getCurrentUserReference())
                .thenThrow(ResourceNotFoundException.class);
        Assertions.assertThatThrownBy(() -> foodService.save(request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(foodRepository, never()).save(any());
    }

    @Test
    void update_shouldReturnFoodResponse() {
        User user = TestEntityFactory.user();
        user.setUserId(TestEntityFactory.DEFAULT_ID);
        Food food = TestEntityFactory.food(TestEntityFactory.foodType());
        food.setProtein(BigDecimal.ONE);
        food.setUser(user);
        String foodName = "New Food Name";
        String typeName = "New Type Name";
        FoodUpdateRequest request = new FoodUpdateRequest(foodName, BigDecimal.ONE, BigDecimal.ONE, 1L);
        FoodType foodType = TestEntityFactory.foodType();
        foodType.setMultiplier(1);
        foodType.setName(typeName);
        when(foodTypeReadService.findByIdOrThrow(request.foodTypeId())).thenReturn(foodType);
        when(foodReadService.findByIdOrThrow(anyLong())).thenReturn(food);
        when(userService.getCurrentUserId()).thenReturn(user.getUserId());
        when(foodRepository.save(any(Food.class))).thenReturn(food);
        FoodResponse response = foodService.update(foodId, request);
        ArgumentCaptor<Food> captor = ArgumentCaptor.forClass(Food.class);
        verify(foodRepository).save(captor.capture());
        Food savedFood = captor.getValue();
        Assertions.assertThat(savedFood.getName()).isEqualTo(foodName);
        Assertions.assertThat(savedFood.getProtein()).isEqualByComparingTo(BigDecimal.ONE);
        Assertions.assertThat(savedFood.getCalories()).isEqualByComparingTo(BigDecimal.ONE);
        Assertions.assertThat(savedFood.getFoodType()).isEqualTo(foodType);
        Assertions.assertThat(savedFood.getPhenylalanine()).isEqualByComparingTo(BigDecimal.ONE);
        doAssertionsCheckOnResponse(response, food);
    }

    @Test
    void update_shouldThrowExceptionAndSaveNothing_whenResourceNotFound() {
        when(foodReadService.findByIdOrThrow(foodId)).thenThrow(ResourceNotFoundException.class);
        FoodUpdateRequest request = new FoodUpdateRequest(null, null, null, null);
        Assertions.assertThatThrownBy(() -> foodService.update(foodId, request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(foodRepository, never()).save(any());
    }

    @Test
    void deleteById_whenFoodExists() {
        User user = TestEntityFactory.user();
        user.setUserId(TestEntityFactory.DEFAULT_ID);
        Food food = TestEntityFactory.food(TestEntityFactory.foodType());
        food.setUser(user);
        when(foodReadService.findByIdOrThrow(foodId)).thenReturn(food);
        when(userService.getCurrentUserId()).thenReturn(user.getUserId());
        foodService.deleteById(foodId);
        verify(foodRepository).delete(any(Food.class));
    }

    @Test
    void deleteById_shouldThrowExceptionAndSaveNothing_whenResourceNotFound() {
        when(foodReadService.findByIdOrThrow(foodId)).thenThrow(ResourceNotFoundException.class);
        Assertions.assertThatThrownBy(() -> foodService.deleteById(foodId))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(foodRepository, never()).delete(any(Food.class));
    }

    private void doAssertionsCheckOnResponse(FoodResponse response, Food food) {
        Assertions.assertThat(response.id()).isEqualTo(food.getId());
        Assertions.assertThat(response.name()).isEqualTo(food.getName());
        Assertions.assertThat(response.calories()).isEqualByComparingTo(food.getCalories());
        Assertions.assertThat(response.phenylalanine()).isEqualByComparingTo(food.getPhenylalanine());
        Assertions.assertThat(response.protein()).isEqualByComparingTo(food.getProtein());
        Assertions.assertThat(response.multiplier()).isEqualTo(food.getFoodType().getMultiplier());
        Assertions.assertThat(response.foodTypeName()).isEqualTo(food.getFoodType().getName());
    }

}
