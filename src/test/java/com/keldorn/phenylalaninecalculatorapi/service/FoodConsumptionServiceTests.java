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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
    private FoodConsumptionMapper foodConsumptionMapper;
    @Mock
    private FoodService foodService;
    @Mock
    private UserService userService;
    @Mock
    private DailyIntakeService dailyIntakeService;

    @InjectMocks
    private FoodConsumptionService foodConsumptionService;

    @Test
    public void FoodConsumptionService_Create_ReturnsFoodConsumptionResponse() {
        Long foodId = 1L;
        BigDecimal foodPheContent = BigDecimal.valueOf(200);
        BigDecimal consumedAmount = BigDecimal.valueOf(50);
        BigDecimal expectedCalculatedPhe = BigDecimal.valueOf(10).setScale(4, RoundingMode.HALF_UP);

        FoodConsumptionRequest request = new FoodConsumptionRequest(consumedAmount);

        User user = TestEntityFactory.user();
        when(userService.getCurrentUser()).thenReturn(user);

        Food food = TestEntityFactory.food(TestEntityFactory.foodType());
        food.setPhenylalanine(foodPheContent);
        when(foodService.findByIdOrThrow(foodId)).thenReturn(food);

        when(foodConsumptionRepository.save(any(FoodConsumption.class)))
                .thenAnswer(i -> i.getArguments()[0]);
        when(foodConsumptionMapper.toResponse(any(FoodConsumption.class)))
                .thenReturn(new FoodConsumptionResponse(1L, BigDecimal.ZERO, BigDecimal.ZERO, null));

        foodConsumptionService.create(foodId, request);

        verify(dailyIntakeService).addAmount(any(LocalDate.class), eq(expectedCalculatedPhe));

        ArgumentCaptor<FoodConsumption> captor = ArgumentCaptor.forClass(FoodConsumption.class);
        verify(foodConsumptionRepository).save(captor.capture());

        FoodConsumption savedEntity = captor.getValue();

        Assertions.assertThat(savedEntity.getPhenylalanineAmount())
                .isEqualByComparingTo(expectedCalculatedPhe);
        Assertions.assertThat(savedEntity.getAmount())
                .isEqualByComparingTo(consumedAmount);
        Assertions.assertThat(savedEntity.getConsumedAt()).isNotNull();
    }

    @Test
    public void FoodConsumptionService_Create_WhenFoodNotFound_ThrowsExceptionAndSavesNothing() {
        Long invalidFoodId = 999L;
        FoodConsumptionRequest request = new FoodConsumptionRequest(BigDecimal.TEN);

        when(foodService.findByIdOrThrow(invalidFoodId))
                .thenThrow(new FoodNotFoundException("Food not found"));

        Assertions.assertThatThrownBy(() -> foodConsumptionService.create(invalidFoodId, request))
                .isInstanceOf(FoodNotFoundException.class);

        verify(dailyIntakeService, never()).addAmount(any(), any());
        verify(foodConsumptionRepository, never()).save(any());
    }

    @Test
    public void FoodConsumptionService_Create_WhenDailyIntakeFailsDueToNegativeConsumption_ThrowsExceptionAndSavesNothing() {
        Long foodId = 1L;
        FoodConsumptionRequest request = new FoodConsumptionRequest(BigDecimal.valueOf(-100));

        User user = TestEntityFactory.user();
        when(userService.getCurrentUser()).thenReturn(user);
        when(foodService.findByIdOrThrow(foodId)).thenReturn(TestEntityFactory.food(TestEntityFactory.foodType()));

        doThrow(new DailyIntakeCannotBeLowerThanZeroException("Daily intake cannot be lower than zero"))
                .when(dailyIntakeService).addAmount(any(), any());

        Assertions.assertThatThrownBy(() -> foodConsumptionService.create(foodId, request))
                .isInstanceOf(DailyIntakeCannotBeLowerThanZeroException.class)
                .hasMessageContaining("Daily intake cannot be lower than zero");

        verify(foodConsumptionRepository, never()).save(any());
    }

    @Test
    public void FoodConsumptionService_FindAllByDate_ReturnsPageOfFoodConsumptionResponses() {
        Long userId = 1L;
        List<FoodConsumption> consumptionList = List.of(new FoodConsumption());
        Page<FoodConsumption> pageWithData = new PageImpl<>(consumptionList);

        when(userService.getCurrentUser()).thenReturn(TestEntityFactory.user());
        when(userService.getCurrentUserId()).thenReturn(userId);
        when(foodConsumptionRepository.findAllByUserAndConsumedAtBetween(any(), any(), any(), any(Pageable.class)))
                .thenReturn(pageWithData);
        when(foodConsumptionMapper.toResponse(any(FoodConsumption.class)))
                .thenReturn(new FoodConsumptionResponse(1L, BigDecimal.ZERO, BigDecimal.ZERO, null));

        Page<FoodConsumptionResponse> allByDate = foodConsumptionService.findAllByDate(TestEntityFactory.TEST_DATE, 0, 20);

        Assertions.assertThat(allByDate).isNotNull();
        Assertions.assertThat(allByDate).isNotEmpty();
    }

    @Test
    public void FoodConsumptionService_FindAllByDate_ReturnsEmptyList() {
        Long userId = 1L;

        when(userService.getCurrentUser()).thenReturn(TestEntityFactory.user());
        when(userService.getCurrentUserId()).thenReturn(userId);
        when(foodConsumptionRepository.findAllByUserAndConsumedAtBetween(any(), any(), any(), any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<FoodConsumptionResponse> allByDate = foodConsumptionService.findAllByDate(TestEntityFactory.TEST_DATE, 0, 20);

        Assertions.assertThat(allByDate).isNotNull();
        Assertions.assertThat(allByDate).isEmpty();
    }

    @Test
    public void FoodConsumptionService_Update_ReturnsFoodConsumptionResponse() {
        Long id = 1L;
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
        when(foodConsumptionRepository.findById(id)).thenReturn(Optional.of(existingEntity));
        when(foodConsumptionRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        when(foodConsumptionMapper.toResponse(any())).thenReturn(null);

        foodConsumptionService.update(id, request);

        verify(dailyIntakeService).addAmount(any(LocalDate.class), eq(expectedDelta));

        ArgumentCaptor<FoodConsumption> captor = ArgumentCaptor.forClass(FoodConsumption.class);
        verify(foodConsumptionRepository).save(captor.capture());

        FoodConsumption savedEntity = captor.getValue();
        Assertions.assertThat(savedEntity.getPhenylalanineAmount()).isEqualByComparingTo(newPheAmount);
        Assertions.assertThat(savedEntity.getAmount()).isEqualByComparingTo(newAmount);
    }

    @Test
    public void FoodConsumptionService_Update_WhenFoodConsumptionNotFound_ThrowsExceptionAndSavesNothing() {
        Long invalidId = 999L;
        FoodConsumptionRequest request = new FoodConsumptionRequest(BigDecimal.TEN);

        when(foodConsumptionRepository.findById(invalidId))
                .thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> foodConsumptionService.update(invalidId, request))
                .isInstanceOf(FoodConsumptionNotFoundException.class);

        verify(dailyIntakeService, never()).addAmount(any(), any());
        verify(foodConsumptionRepository, never()).save(any());
    }

    @Test
    public void FoodConsumptionService_Update_WhenDailyIntakeFailsDueToNegativeConsumption_ThrowsExceptionAndSavesNothing() {
        Long id = 1L;
        FoodConsumptionRequest request = new FoodConsumptionRequest(BigDecimal.valueOf(-100));

        User user = TestEntityFactory.user();
        FoodConsumption existingEntity = TestEntityFactory.foodConsumption(
                user,
                TestEntityFactory.food(TestEntityFactory.foodType()),
                TestEntityFactory.CONSUMED_AT
        );

        when(foodConsumptionRepository.findById(id)).thenReturn(Optional.of(existingEntity));
        when(userService.getCurrentUser()).thenReturn(user);

        doThrow(new DailyIntakeCannotBeLowerThanZeroException("Daily intake cannot be lower than zero"))
                .when(dailyIntakeService).addAmount(any(), any());

        Assertions.assertThatThrownBy(() -> foodConsumptionService.update(id, request))
                .isInstanceOf(DailyIntakeCannotBeLowerThanZeroException.class);

        verify(foodConsumptionRepository, never()).save(any());
    }

    @Test
    public void FoodConsumptionService_DeleteById_RemovesAmountFromDailyIntakeAndDeletesEntity() {
        Long id = 1L;
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
        when(foodConsumptionRepository.findById(id)).thenReturn(Optional.of(existingEntity));

        foodConsumptionService.deleteById(id);

        verify(dailyIntakeService).addAmount(any(LocalDate.class), eq(expectedNegativeAmount));
        verify(foodConsumptionRepository).delete(existingEntity);
    }

    @Test
    public void FoodConsumptionService_DeleteById_WhenDailyIntakeFailsDueToNegativeConsumption_RemovesAmountFromDailyIntakeAndDeletesEntity() {
        Long id = 1L;
        User user = TestEntityFactory.user();

        FoodConsumption existingEntity = TestEntityFactory.foodConsumption(
                user,
                TestEntityFactory.food(TestEntityFactory.foodType()),
                TestEntityFactory.CONSUMED_AT
        );

        when(userService.getCurrentUser()).thenReturn(user);
        when(foodConsumptionRepository.findById(id)).thenReturn(Optional.of(existingEntity));

        doThrow(new DailyIntakeCannotBeLowerThanZeroException("Daily intake cannot be lower than zero"))
                .when(dailyIntakeService).addAmount(any(), any());

        Assertions.assertThatThrownBy(() -> foodConsumptionService.deleteById(id))
                .isInstanceOf(DailyIntakeCannotBeLowerThanZeroException.class);
        verify(foodConsumptionRepository, never()).delete(any());
    }

    @Test
    public void FoodConsumptionService_DeleteById_WhenFoodConsumptionNotFound_ThrowsExceptionAndSavesNothing() {
        Long invalidId = 999L;

        when(foodConsumptionRepository.findById(invalidId)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> foodConsumptionService.deleteById(invalidId))
                .isInstanceOf(FoodConsumptionNotFoundException.class)
                .hasMessageContaining("Food consumption not found by id: " + invalidId);

        verify(dailyIntakeService, never()).addAmount(any(), any());
        verify(foodConsumptionRepository, never()).delete(any());
    }
}
