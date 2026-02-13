package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.Food;
import com.keldorn.phenylalaninecalculatorapi.domain.entity.FoodConsumption;
import com.keldorn.phenylalaninecalculatorapi.domain.entity.User;
import com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption.FoodConsumptionRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption.FoodConsumptionResponse;
import com.keldorn.phenylalaninecalculatorapi.exception.conflict.DailyIntakeCannotBeLowerThanZeroException;
import com.keldorn.phenylalaninecalculatorapi.exception.notfound.FoodConsumptionNotFoundException;
import com.keldorn.phenylalaninecalculatorapi.exception.notfound.FoodNotFoundException;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;
import com.keldorn.phenylalaninecalculatorapi.mapper.FoodConsumptionMapper;
import com.keldorn.phenylalaninecalculatorapi.repository.FoodConsumptionRepository;
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
import java.math.RoundingMode;
import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FoodConsumptionServiceTests {

    @Mock
    private FoodConsumptionRepository foodConsumptionRepository;
    @Mock
    private FoodService foodService;
    @Mock
    private UserService userService;
    @Mock
    private DailyIntakeService dailyIntakeService;
    @Spy
    private FoodConsumptionMapper foodConsumptionMapper = Mappers.getMapper(FoodConsumptionMapper.class);

    @InjectMocks
    private FoodConsumptionService foodConsumptionService;

    private final Long FOOD_CONSUMPTION_ID = 1L;

    @Test
    public void save_shouldReturnFoodConsumptionResponse() {
        Long foodId = 1L;
        BigDecimal foodPheContent = BigDecimal.valueOf(200);
        BigDecimal consumedAmount = BigDecimal.valueOf(50);
        BigDecimal expectedCalculatedPhe = BigDecimal.valueOf(10).setScale(4, RoundingMode.HALF_UP);

        FoodConsumptionRequest request = new FoodConsumptionRequest(consumedAmount);
        User user = TestEntityFactory.user();
        Food food = TestEntityFactory.food(TestEntityFactory.foodType());
        food.setPhenylalanine(foodPheContent);

        when(userService.getCurrentUser()).thenReturn(user);
        when(foodService.findByIdOrThrow(foodId)).thenReturn(food);
        when(foodConsumptionRepository.save(any(FoodConsumption.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        FoodConsumptionResponse response = foodConsumptionService.save(foodId, request);

        verify(dailyIntakeService).addAmount(any(LocalDate.class), eq(expectedCalculatedPhe));

        ArgumentCaptor<FoodConsumption> captor = ArgumentCaptor.forClass(FoodConsumption.class);
        verify(foodConsumptionRepository).save(captor.capture());
        verify(foodConsumptionMapper).toResponse(any(FoodConsumption.class));

        FoodConsumption savedEntity = captor.getValue();

        Assertions.assertThat(savedEntity.getPhenylalanineAmount()).isEqualByComparingTo(expectedCalculatedPhe);
        Assertions.assertThat(savedEntity.getAmount()).isEqualByComparingTo(consumedAmount);
        Assertions.assertThat(savedEntity.getConsumedAt()).isNotNull();
        doAssertionsCheckOnResponse(response, savedEntity);
    }

    @Test
    public void save_shouldThrowExceptionAndSaveNothing_whenFoodNotFound() {
        FoodConsumptionRequest request = new FoodConsumptionRequest(BigDecimal.TEN);

        when(foodService.findByIdOrThrow(FOOD_CONSUMPTION_ID))
                .thenThrow(FoodNotFoundException.class);

        Assertions.assertThatThrownBy(() -> foodConsumptionService.save(FOOD_CONSUMPTION_ID, request))
                .isInstanceOf(FoodNotFoundException.class);

        verify(dailyIntakeService, never()).addAmount(any(), any());
        verify(foodConsumptionRepository, never()).save(any());
    }

    @Test
    public void save_shouldThrowExceptionAndSaveNothing_whenDailyIntakeFailsDueToNegativeConsumption() {
        Long foodId = 1L;
        FoodConsumptionRequest request = new FoodConsumptionRequest(BigDecimal.valueOf(-100));

        User user = TestEntityFactory.user();
        when(userService.getCurrentUser()).thenReturn(user);
        when(foodService.findByIdOrThrow(foodId)).thenReturn(TestEntityFactory.food(TestEntityFactory.foodType()));

        doThrow(DailyIntakeCannotBeLowerThanZeroException.class)
                .when(dailyIntakeService).addAmount(any(), any());

        Assertions.assertThatThrownBy(() -> foodConsumptionService.save(foodId, request))
                .isInstanceOf(DailyIntakeCannotBeLowerThanZeroException.class);

        verify(foodConsumptionRepository, never()).save(any());
    }

    @Test
    public void findAllByDate_shouldReturnPageOfFoodConsumptionResponses() {
        Long userId = 1L;
        FoodConsumption foodConsumption = TestEntityFactory.foodConsumption(
                TestEntityFactory.user(),
                TestEntityFactory.food(TestEntityFactory.foodType()),
                TestEntityFactory.CONSUMED_AT
        );
        List<FoodConsumption> consumptionList = List.of(foodConsumption);
        Page<FoodConsumption> pageWithData = new PageImpl<>(consumptionList);

        when(userService.getCurrentUser()).thenReturn(TestEntityFactory.user());
        when(userService.getCurrentUserId()).thenReturn(userId);
        when(foodConsumptionRepository.findAllByUserAndConsumedAtBetween(any(Long.class), any(Instant.class), any(Instant.class), any(Pageable.class)))
                .thenReturn(pageWithData);

        Page<FoodConsumptionResponse> response = foodConsumptionService.findAllByDate(TestEntityFactory.TEST_DATE, 0, 20);

        verify(foodConsumptionMapper).toResponse(consumptionList.getFirst());

        Assertions.assertThat(response).hasSize(1);
        doAssertionsCheckOnResponse(response.getContent().getFirst(), foodConsumption);
    }

    @Test
    public void findAllByDate_shouldReturnsEmptyList() {
        Long userId = 1L;

        when(userService.getCurrentUser()).thenReturn(TestEntityFactory.user());
        when(userService.getCurrentUserId()).thenReturn(userId);
        when(foodConsumptionRepository.findAllByUserAndConsumedAtBetween(any(Long.class), any(Instant.class), any(Instant.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<FoodConsumptionResponse> response = foodConsumptionService.findAllByDate(TestEntityFactory.TEST_DATE, 0, 20);

        Assertions.assertThat(response).hasSize(0);
    }

    @Test
    public void update_shouldReturnFoodConsumptionResponse_whenFoodConsumptionExists() {
        BigDecimal foodPheContent = BigDecimal.valueOf(200);

        BigDecimal oldAmount = BigDecimal.valueOf(25);
        BigDecimal oldPheAmount = BigDecimal.valueOf(5).setScale(4, RoundingMode.HALF_UP);

        BigDecimal newAmount = BigDecimal.valueOf(50);
        BigDecimal newPheAmount = BigDecimal.valueOf(10).setScale(4, RoundingMode.HALF_UP);

        BigDecimal expectedDelta = newPheAmount.subtract(oldPheAmount);

        FoodConsumptionRequest request = new FoodConsumptionRequest(newAmount);
        User user = TestEntityFactory.user();

        FoodConsumption existingEntity = TestEntityFactory.foodConsumption(
                user,
                TestEntityFactory.food(TestEntityFactory.foodType()),
                TestEntityFactory.CONSUMED_AT
        );
        existingEntity.setAmount(oldAmount);
        existingEntity.setPhenylalanineAmount(oldPheAmount);
        existingEntity.getFood().setPhenylalanine(foodPheContent);

        when(userService.getCurrentUser()).thenReturn(user);
        when(foodConsumptionRepository.findById(FOOD_CONSUMPTION_ID)).thenReturn(Optional.of(existingEntity));
        when(foodConsumptionRepository.save(any(FoodConsumption.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        FoodConsumptionResponse response = foodConsumptionService.update(FOOD_CONSUMPTION_ID, request);

        verify(dailyIntakeService).addAmount(any(LocalDate.class), eq(expectedDelta));

        ArgumentCaptor<FoodConsumption> captor = ArgumentCaptor.forClass(FoodConsumption.class);
        verify(foodConsumptionRepository).save(captor.capture());
        verify(foodConsumptionMapper).toResponse(any());

        FoodConsumption savedEntity = captor.getValue();
        Assertions.assertThat(savedEntity.getPhenylalanineAmount()).isEqualByComparingTo(newPheAmount);
        Assertions.assertThat(savedEntity.getAmount()).isEqualByComparingTo(newAmount);
        doAssertionsCheckOnResponse(response, savedEntity);
    }

    @Test
    public void update_shouldThrowExceptionAndSaveNothing_whenFoodConsumptionNotFound() {
        FoodConsumptionRequest request = new FoodConsumptionRequest(BigDecimal.TEN);

        when(foodConsumptionRepository.findById(FOOD_CONSUMPTION_ID))
                .thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> foodConsumptionService.update(FOOD_CONSUMPTION_ID, request))
                .isInstanceOf(FoodConsumptionNotFoundException.class);

        verify(dailyIntakeService, never()).addAmount(any(), any());
        verify(foodConsumptionRepository, never()).save(any());
    }

    @Test
    public void update_shouldThrowExceptionAndSaveNothing_whenDailyIntakeFailsDueToNegativeConsumption() {
        FoodConsumptionRequest request = new FoodConsumptionRequest(BigDecimal.valueOf(-100));

        User user = TestEntityFactory.user();
        FoodConsumption existingEntity = TestEntityFactory.foodConsumption(
                user,
                TestEntityFactory.food(TestEntityFactory.foodType()),
                TestEntityFactory.CONSUMED_AT
        );

        when(foodConsumptionRepository.findById(FOOD_CONSUMPTION_ID)).thenReturn(Optional.of(existingEntity));
        when(userService.getCurrentUser()).thenReturn(user);

        doThrow(DailyIntakeCannotBeLowerThanZeroException.class)
                .when(dailyIntakeService).addAmount(any(), any());

        Assertions.assertThatThrownBy(() -> foodConsumptionService.update(FOOD_CONSUMPTION_ID, request))
                .isInstanceOf(DailyIntakeCannotBeLowerThanZeroException.class);

        verify(foodConsumptionRepository, never()).save(any());
    }

    @Test
    public void deleteById_shouldRemoveAmountFromDailyIntakeAndDeleteEntity() {
        BigDecimal currentPheAmount = BigDecimal.valueOf(20).setScale(4, RoundingMode.HALF_UP);

        BigDecimal expectedNegativeAmount = currentPheAmount.negate();

        User user = TestEntityFactory.user();

        FoodConsumption existingEntity = TestEntityFactory.foodConsumption(
                user,
                TestEntityFactory.food(TestEntityFactory.foodType()),
                TestEntityFactory.CONSUMED_AT
        );
        existingEntity.setPhenylalanineAmount(currentPheAmount);

        when(userService.getCurrentUser()).thenReturn(user);
        when(foodConsumptionRepository.findById(FOOD_CONSUMPTION_ID)).thenReturn(Optional.of(existingEntity));

        foodConsumptionService.deleteById(FOOD_CONSUMPTION_ID);

        verify(dailyIntakeService).addAmount(any(LocalDate.class), eq(expectedNegativeAmount));
        verify(foodConsumptionRepository).delete(existingEntity);
    }

    @Test
    public void deleteById_shouldRemoveAmountFromDailyIntakeAndDeleteEntity_whenDailyIntakeFailsDueToNegativeConsumption() {
        User user = TestEntityFactory.user();

        FoodConsumption existingEntity = TestEntityFactory.foodConsumption(
                user,
                TestEntityFactory.food(TestEntityFactory.foodType()),
                TestEntityFactory.CONSUMED_AT
        );

        when(userService.getCurrentUser()).thenReturn(user);
        when(foodConsumptionRepository.findById(FOOD_CONSUMPTION_ID)).thenReturn(Optional.of(existingEntity));

        doThrow(DailyIntakeCannotBeLowerThanZeroException.class)
                .when(dailyIntakeService).addAmount(any(), any());

        Assertions.assertThatThrownBy(() -> foodConsumptionService.deleteById(FOOD_CONSUMPTION_ID))
                .isInstanceOf(DailyIntakeCannotBeLowerThanZeroException.class);
        verify(foodConsumptionRepository, never()).delete(any());
    }

    @Test
    public void deleteById_shouldThrowExceptionAndSaveNothing_whenFoodConsumptionNotFound() {
        when(foodConsumptionRepository.findById(FOOD_CONSUMPTION_ID)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> foodConsumptionService.deleteById(FOOD_CONSUMPTION_ID))
                .isInstanceOf(FoodConsumptionNotFoundException.class);

        verify(dailyIntakeService, never()).addAmount(any(), any());
        verify(foodConsumptionRepository, never()).delete(any());
    }

    private void doAssertionsCheckOnResponse(FoodConsumptionResponse response, FoodConsumption foodConsumption) {
        Assertions.assertThat(response.id()).isEqualTo(foodConsumption.getId());
        Assertions.assertThat(response.amount()).isEqualTo(foodConsumption.getAmount());
        Assertions.assertThat(response.consumedAt()).isNotNull();
        Assertions.assertThat(response.phenylalanineAmount()).isEqualByComparingTo(foodConsumption.getPhenylalanineAmount());
    }
}
