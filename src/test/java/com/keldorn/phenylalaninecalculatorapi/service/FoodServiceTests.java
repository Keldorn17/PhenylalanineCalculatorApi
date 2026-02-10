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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
    private FoodMapper foodMapper;
    @Mock
    private FoodTypeService foodTypeService;
    @Mock
    private UserService userService;

    @InjectMocks
    private FoodService foodService;

    @Test
    public void FoodService_FindById_WhenFoodNotFound_ThrowsFoodNotFoundException() {
        Long foodId = 999L;

        when(foodRepository.findById(foodId)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> foodService.findById(foodId))
                .isInstanceOf(FoodNotFoundException.class)
                .hasMessageContaining("Food Not Found.");
    }

    @Test
    public void FoodService_FindById_ReturnsFoodResponse() {
        Long foodId = 1L;
        Food food = TestEntityFactory.food(TestEntityFactory.foodType());
        FoodResponse foodResponse = new FoodResponse(
                foodId,
                TestEntityFactory.DEFAULT_FOOD_NAME,
                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE,
                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE,
                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE,
                TestEntityFactory.DEFAULT_FOOD_TYPE_NAME,
                TestEntityFactory.DEFAULT_INTEGER_VALUE
        );

        when(foodRepository.findById(foodId)).thenReturn(Optional.of(food));
        when(foodMapper.toResponse(any(Food.class))).thenReturn(foodResponse);

        FoodResponse response = foodService.findById(foodId);

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.name()).isEqualTo(TestEntityFactory.DEFAULT_FOOD_NAME);
    }

    @Test
    public void FoodService_FindAll_ReturnsPageOfFoodResponses() {
        List<Food> responseList = List.of(new Food());
        Page<Food> responsePage = new PageImpl<>(responseList);

        when(foodRepository.findAll(any(Pageable.class))).thenReturn(responsePage);
        when(foodMapper.toResponse(any(Food.class)))
                .thenReturn(new FoodResponse(1L, null, null, null, null, null, 1));

        Page<FoodResponse> response = foodService.findAll(0, 20);

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response).isNotEmpty();
        Assertions.assertThat(response.getContent()).hasSize(1);
    }

    @Test
    public void FoodService_FindAll_ReturnsEmptyList() {
        when(foodRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        Page<FoodResponse> response = foodService.findAll(0, 20);

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response).isEmpty();
    }

    @Test
    public void FoodService_Save_ReturnsFoodResponse() {
        Long foodTypeId = 1L;
        FoodRequest request = new FoodRequest(null, BigDecimal.TEN, BigDecimal.TEN, 1L);
        FoodType foodType = TestEntityFactory.foodType();
        String updatedName = "Updated Type Name";
        foodType.setName(updatedName);
        Food food = TestEntityFactory.food(TestEntityFactory.foodType());

        when(foodMapper.toEntity(any(FoodRequest.class))).thenReturn(food);
        when(foodTypeService.findByIdOrThrow(foodTypeId)).thenReturn(foodType);
        when(userService.getCurrentUser()).thenReturn(TestEntityFactory.user());
        when(foodRepository.save(any(Food.class))).thenReturn(food);
        when(foodMapper.toResponse(any(Food.class))).thenReturn(
                new FoodResponse(1L, null, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, "Updated", 10)
        );

        foodService.save(request);

        ArgumentCaptor<Food> captor = ArgumentCaptor.forClass(Food.class);
        verify(foodRepository).save(captor.capture());

        Food savedFood = captor.getValue();

        Assertions.assertThat(savedFood).isNotNull();
        Assertions.assertThat(savedFood.getUser()).isNotNull();
        Assertions.assertThat(savedFood.getFoodType()).isNotNull();
        Assertions.assertThat(savedFood.getPhenylalanine())
                .isEqualByComparingTo(food.getProtein()
                        .multiply(BigDecimal.valueOf(food.getFoodType().getMultiplier())));
        Assertions.assertThat(savedFood.getFoodType().getName()).isEqualTo(updatedName);
    }

    @Test
    public void FoodService_Save_WhenFoodTypeNotFound_ThrowsExceptionAndSavesNothing() {
        FoodRequest request = new FoodRequest(null, null, null, 1L);

        when(foodMapper.toEntity(any(FoodRequest.class))).thenReturn(TestEntityFactory.food(TestEntityFactory.foodType()));
        when(foodTypeService.findByIdOrThrow(any(Long.class)))
                .thenThrow(FoodTypeNotFoundException.class);

        Assertions.assertThatThrownBy(() -> foodService.save(request))
                .isInstanceOf(FoodTypeNotFoundException.class);

        verify(foodRepository, never()).save(any());
    }

    @Test
    public void FoodService_Save_WhenUserNotFound_ThrowsExceptionAndSavesNothing() {
        FoodRequest request = new FoodRequest(null, null, null, 1L);

        when(foodMapper.toEntity(any(FoodRequest.class))).thenReturn(TestEntityFactory.food(TestEntityFactory.foodType()));
        when(foodTypeService.findByIdOrThrow(any(Long.class)))
                .thenReturn(TestEntityFactory.foodType());
        when(userService.getCurrentUser())
                .thenThrow(UserNotFoundException.class);

        Assertions.assertThatThrownBy(() -> foodService.save(request))
                        .isInstanceOf(UserNotFoundException.class);

        verify(foodRepository, never()).save(any());
    }

    @Test
    public void FoodService_Update_ReturnsFoodResponse() {
        Long foodId = 1L;
        Food food = TestEntityFactory.food(TestEntityFactory.foodType());
        String foodName = "New Food Name";
        String typeName = "New Type Name";
        FoodUpdateRequest request = new FoodUpdateRequest(foodName, BigDecimal.ONE, BigDecimal.ONE, 1L);
        FoodType foodType = TestEntityFactory.foodType();
        foodType.setName(typeName);

        when(foodRepository.findById(foodId)).thenReturn(Optional.of(food));
        when(foodTypeService.findByIdOrThrow(any(Long.class))).thenReturn(foodType);
        when(foodRepository.save(any(Food.class))).thenReturn(food);
        when(foodMapper.toResponse(any(Food.class))).thenReturn(null);

        foodService.update(foodId, request);

        ArgumentCaptor<Food> captor = ArgumentCaptor.forClass(Food.class);
        verify(foodRepository).save(captor.capture());

        Food savedFood = captor.getValue();

        Assertions.assertThat(savedFood).isNotNull();
        Assertions.assertThat(savedFood.getName()).isEqualTo(foodName);
        Assertions.assertThat(savedFood.getProtein()).isEqualByComparingTo(BigDecimal.ONE);
        Assertions.assertThat(savedFood.getCalories()).isEqualByComparingTo(BigDecimal.ONE);
        Assertions.assertThat(savedFood.getFoodType()).isEqualTo(foodType);
        Assertions.assertThat(savedFood.getPhenylalanine())
                .isEqualByComparingTo(food.getProtein()
                        .multiply(BigDecimal.valueOf(food.getFoodType().getMultiplier())));
    }

    @Test
    public void FoodService_Update_WhenFoodNotFound_ThrowsExceptionAndSavesNothing() {
        Long foodId = 1L;

        when(foodRepository.findById(foodId))
                .thenThrow(FoodNotFoundException.class);

        Assertions.assertThatThrownBy(() ->
                        foodService.update(foodId, new FoodUpdateRequest(null, null, null, null)))
                .isInstanceOf(FoodNotFoundException.class);

        verify(foodRepository, never()).save(any());
    }

    @Test
    public void FoodService_DeleteById() {
        Long foodId = 1L;

        when(foodRepository.findById(foodId))
                .thenReturn(Optional.of(TestEntityFactory.food(TestEntityFactory.foodType())));

        foodService.deleteById(foodId);

        verify(foodRepository).delete(any());
    }

    @Test
    public void FoodService_DeleteById_WhenFoodNotFound_ThrowsExceptionAndSavesNothing() {
        Long foodId = 1L;

        when(foodRepository.findById(foodId))
                .thenThrow(FoodNotFoundException.class);

        Assertions.assertThatThrownBy(() -> foodService.deleteById(foodId))
                        .isInstanceOf(FoodNotFoundException.class);

        verify(foodRepository, never()).delete(any());
    }
}
