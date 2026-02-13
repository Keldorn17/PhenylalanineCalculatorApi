package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.Food;
import com.keldorn.phenylalaninecalculatorapi.domain.entity.FoodType;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodUpdateRequest;
import com.keldorn.phenylalaninecalculatorapi.exception.notfound.FoodNotFoundException;
import com.keldorn.phenylalaninecalculatorapi.exception.notfound.FoodTypeNotFoundException;
import com.keldorn.phenylalaninecalculatorapi.exception.notfound.UserNotFoundException;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;
import com.keldorn.phenylalaninecalculatorapi.mapper.FoodMapper;
import com.keldorn.phenylalaninecalculatorapi.repository.FoodRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FoodServiceTests {

    @Mock
    private FoodRepository foodRepository;
    @Mock
    private FoodTypeService foodTypeService;
    @Mock
    private UserService userService;
    @Spy
    private FoodMapper foodMapper = Mappers.getMapper(FoodMapper.class);

    @InjectMocks
    private FoodService foodService;

    private final Long FOOD_ID = 1L;

    @Test
    public void findById_shouldThrowFoodNotFoundException_whenFoodNotFound() {
        when(foodRepository.findById(FOOD_ID)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> foodService.findById(FOOD_ID))
                .isInstanceOf(FoodNotFoundException.class);
    }

    @Test
    public void findById_shouldReturnFoodResponse_whenFoodExists() {
        Food food = TestEntityFactory.food(TestEntityFactory.foodType());

        when(foodRepository.findById(FOOD_ID)).thenReturn(Optional.of(food));

        FoodResponse response = foodService.findById(FOOD_ID);

        verify(foodMapper).toResponse(food);

        doAssertionsCheckOnResponse(response, food);
    }

    @Test
    public void findAll_shouldReturnPageOfFoodResponses() {
        Food food = TestEntityFactory.food(TestEntityFactory.foodType());
        List<Food> responseList = List.of(food);
        Page<Food> responsePage = new PageImpl<>(responseList);

        when(foodRepository.findAll(any(Pageable.class))).thenReturn(responsePage);

        Page<FoodResponse> response = foodService.findAll(0, 20);

        verify(foodMapper).toResponse(any(Food.class));

        Assertions.assertThat(response.getContent()).hasSize(1);
        doAssertionsCheckOnResponse(response.getContent().getFirst(), food);
    }

    @Test
    public void findAll_shouldReturnEmptyList() {
        when(foodRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        Page<FoodResponse> response = foodService.findAll(0, 20);

        Assertions.assertThat(response).hasSize(0);
    }

    @Test
    public void save_shouldReturnsFoodResponse() {
        Long foodTypeId = 1L;
        FoodRequest request = new FoodRequest("Test Name", BigDecimal.TEN, BigDecimal.TEN, 1L);
        FoodType foodType = TestEntityFactory.foodType();
        foodType.setName("Updated Type Name");
        Food food = TestEntityFactory.food(TestEntityFactory.foodType());

        when(foodTypeService.findByIdOrThrow(foodTypeId)).thenReturn(foodType);
        when(userService.getCurrentUser()).thenReturn(TestEntityFactory.user());
        when(foodRepository.save(any(Food.class))).thenReturn(food);

        FoodResponse response = foodService.save(request);

        verify(foodMapper).toEntity(request);
        verify(foodMapper).toResponse(food);

        ArgumentCaptor<Food> captor = ArgumentCaptor.forClass(Food.class);
        verify(foodRepository).save(captor.capture());

        Food savedFood = captor.getValue();

        Assertions.assertThat(savedFood.getUser()).isNotNull();
        Assertions.assertThat(savedFood.getFoodType()).isNotNull();
        Assertions.assertThat(savedFood.getPhenylalanine())
                .isEqualByComparingTo(food.getProtein()
                        .multiply(BigDecimal.valueOf(food.getFoodType().getMultiplier())));
        Assertions.assertThat(savedFood.getFoodType().getName()).isEqualTo(foodType.getName());
        doAssertionsCheckOnResponse(response, food);
    }

    @Test
    public void save_shouldThrowExceptionAndSaveNothing_whenFoodTypeNotFound() {
        FoodRequest request = new FoodRequest(null, null, null, 1L);

        when(foodTypeService.findByIdOrThrow(request.foodTypeId()))
                .thenThrow(FoodTypeNotFoundException.class);


        Assertions.assertThatThrownBy(() -> foodService.save(request))
                .isInstanceOf(FoodTypeNotFoundException.class);

        verify(foodMapper).toEntity(request);
        verify(foodMapper, never()).toResponse(any());
        verify(foodRepository, never()).save(any());
    }

    @Test
    public void save_shouldThrowExceptionAndSaveNothing_whenUserNotFound() {
        FoodRequest request = new FoodRequest(null, null, null, 1L);

        when(foodTypeService.findByIdOrThrow(request.foodTypeId()))
                .thenReturn(TestEntityFactory.foodType());
        when(userService.getCurrentUser())
                .thenThrow(UserNotFoundException.class);

        Assertions.assertThatThrownBy(() -> foodService.save(request))
                        .isInstanceOf(UserNotFoundException.class);

        verify(foodMapper).toEntity(request);
        verify(foodMapper, never()).toResponse(any());
        verify(foodRepository, never()).save(any());
    }

    @Test
    public void update_shouldReturnFoodResponse() {
        Food food = TestEntityFactory.food(TestEntityFactory.foodType());
        String foodName = "New Food Name";
        String typeName = "New Type Name";
        FoodUpdateRequest request = new FoodUpdateRequest(foodName, BigDecimal.ONE, BigDecimal.ONE, 1L);
        FoodType foodType = TestEntityFactory.foodType();
        foodType.setName(typeName);

        when(foodRepository.findById(FOOD_ID)).thenReturn(Optional.of(food));
        when(foodTypeService.findByIdOrThrow(request.foodTypeId())).thenReturn(foodType);
        when(foodRepository.save(any(Food.class))).thenReturn(food);

        FoodResponse response = foodService.update(FOOD_ID, request);

        ArgumentCaptor<Food> captor = ArgumentCaptor.forClass(Food.class);
        verify(foodRepository).save(captor.capture());
        verify(foodMapper).toResponse(any(Food.class));
        verify(foodMapper, never()).toEntity(any());

        Food savedFood = captor.getValue();

        Assertions.assertThat(savedFood.getName()).isEqualTo(foodName);
        Assertions.assertThat(savedFood.getProtein()).isEqualByComparingTo(BigDecimal.ONE);
        Assertions.assertThat(savedFood.getCalories()).isEqualByComparingTo(BigDecimal.ONE);
        Assertions.assertThat(savedFood.getFoodType()).isEqualTo(foodType);
        Assertions.assertThat(savedFood.getPhenylalanine())
                .isEqualByComparingTo(food.getProtein()
                        .multiply(BigDecimal.valueOf(food.getFoodType().getMultiplier())));
        doAssertionsCheckOnResponse(response, food);
    }

    @Test
    public void update_shouldThrowExceptionAndSaveNothing_whenFoodNotFound() {
        when(foodRepository.findById(FOOD_ID))
                .thenThrow(FoodNotFoundException.class);

        Assertions.assertThatThrownBy(() ->
                        foodService.update(FOOD_ID, new FoodUpdateRequest(null, null, null, null)))
                .isInstanceOf(FoodNotFoundException.class);

        verify(foodRepository, never()).save(any());
    }

    @Test
    public void deleteById_whenFoodExists() {
        when(foodRepository.findById(FOOD_ID))
                .thenReturn(Optional.of(TestEntityFactory.food(TestEntityFactory.foodType())));

        foodService.deleteById(FOOD_ID);

        verify(foodRepository).delete(any());
    }

    @Test
    public void deleteById_shouldThrowExceptionAndSaveNothing_whenFoodNotFound() {
        when(foodRepository.findById(FOOD_ID))
                .thenThrow(FoodNotFoundException.class);

        Assertions.assertThatThrownBy(() -> foodService.deleteById(FOOD_ID))
                        .isInstanceOf(FoodNotFoundException.class);

        verify(foodRepository, never()).delete(any());
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
