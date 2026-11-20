package com.keldorn.phenylalaninecalculatorapi.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.Food;
import com.keldorn.phenylalaninecalculatorapi.domain.entity.FoodConsumption;
import com.keldorn.phenylalaninecalculatorapi.domain.entity.User;
import com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption.FoodConsumptionCreateRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption.FoodConsumptionRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption.FoodConsumptionResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption.PagedFoodConsumptionResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.params.PaginationRequest;
import com.keldorn.phenylalaninecalculatorapi.exception.ResourceNotFoundException;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;
import com.keldorn.phenylalaninecalculatorapi.repository.FoodConsumptionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class FoodConsumptionServiceTests {

    @Mock
    private FoodConsumptionRepository foodConsumptionRepository;

    @Mock
    private FoodReadService foodReadService;

    @Mock
    private UserService userService;

    @Mock
    private ObjectProvider<FoodConsumptionService> selfProvider;

    @InjectMocks
    private FoodConsumptionService foodConsumptionService;

    @BeforeEach
    void setUp() {
        lenient().when(selfProvider.getObject()).thenReturn(foodConsumptionService);
    }

    private final Long foodConsumptionId = 1L;

    @Test
    void save_shouldReturnFoodConsumptionResponse() {
        Long foodId = 1L;
        BigDecimal foodPheContent = BigDecimal.valueOf(200);
        BigDecimal consumedAmount = BigDecimal.valueOf(50);
        BigDecimal expectedCalculatedPhe = BigDecimal.valueOf(100).setScale(4, RoundingMode.HALF_UP);
        FoodConsumptionCreateRequest request = new FoodConsumptionCreateRequest(foodId, consumedAmount);
        User user = TestEntityFactory.user();
        Food food = TestEntityFactory.food(TestEntityFactory.foodType());
        food.setId(foodId);
        food.setPhenylalanine(foodPheContent);
        when(userService.getCurrentUserId()).thenReturn(TestEntityFactory.DEFAULT_ID);
        when(userService.getCurrentUserReference()).thenReturn(user);
        when(foodReadService.findByIdOrThrow(foodId, TestEntityFactory.DEFAULT_ID)).thenReturn(food);
        when(foodConsumptionRepository.save(any(FoodConsumption.class)))
                .thenAnswer(i -> i.getArguments()[0]);
        FoodConsumptionResponse response = foodConsumptionService.save(request);
        ArgumentCaptor<FoodConsumption> captor = ArgumentCaptor.forClass(FoodConsumption.class);
        verify(foodConsumptionRepository).save(captor.capture());
        FoodConsumption savedEntity = captor.getValue();
        Assertions.assertThat(savedEntity.getPhenylalanineAmount()).isEqualByComparingTo(expectedCalculatedPhe);
        Assertions.assertThat(savedEntity.getAmount()).isEqualByComparingTo(consumedAmount);
        Assertions.assertThat(savedEntity.getConsumedAt()).isNotNull();
        doAssertionsCheckOnResponse(response, savedEntity);
    }

    @Test
    void save_shouldThrowExceptionAndSaveNothing_whenResourceNotFound() {
        FoodConsumptionCreateRequest request = new FoodConsumptionCreateRequest(foodConsumptionId, BigDecimal.TEN);
        when(userService.getCurrentUserId()).thenReturn(TestEntityFactory.DEFAULT_ID);
        when(foodReadService.findByIdOrThrow(foodConsumptionId, TestEntityFactory.DEFAULT_ID))
                .thenThrow(ResourceNotFoundException.class);
        Assertions.assertThatThrownBy(() -> foodConsumptionService.save(request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(foodConsumptionRepository, never()).save(any());
    }

    @Test
    void findAllByDate_shouldReturnPageOfFoodConsumptionResponses() {
        Long userId = 1L;
        PaginationRequest paginationRequest = new PaginationRequest(0, 20);
        Food food = TestEntityFactory.food(TestEntityFactory.foodType());
        food.setId(TestEntityFactory.DEFAULT_ID);
        FoodConsumption foodConsumption = TestEntityFactory.foodConsumption(
                TestEntityFactory.user(),
                food,
                TestEntityFactory.CONSUMED_AT
        );
        List<FoodConsumption> consumptionList = List.of(foodConsumption);
        Page<FoodConsumption> pageWithData = new PageImpl<>(consumptionList);
        when(userService.getCurrentUserId()).thenReturn(userId);
        when(foodConsumptionRepository.findAllByUserAndConsumedAtBetween(any(Long.class), any(ZonedDateTime.class),
                any(ZonedDateTime.class), any(Pageable.class)))
                .thenReturn(pageWithData);
        PagedFoodConsumptionResponse response =
                foodConsumptionService.findAllByDate(TestEntityFactory.TEST_DATE, paginationRequest);
        Assertions.assertThat(response.getContent()).hasSize(1);
        doAssertionsCheckOnResponse(response.getContent().getFirst(), foodConsumption);
    }

    @Test
    void findAllByDate_shouldReturnsEmptyList() {
        Long userId = 1L;
        PaginationRequest paginationRequest = new PaginationRequest(0, 20);
        when(userService.getCurrentUserId()).thenReturn(userId);
        when(foodConsumptionRepository.findAllByUserAndConsumedAtBetween(any(Long.class), any(ZonedDateTime.class),
                any(ZonedDateTime.class), any(Pageable.class)))
                .thenReturn(Page.empty());
        PagedFoodConsumptionResponse response =
                foodConsumptionService.findAllByDate(TestEntityFactory.TEST_DATE, paginationRequest);
        Assertions.assertThat(response.getContent()).isEmpty();
    }

    @Test
    void update_shouldReturnFoodConsumptionResponse_whenFoodConsumptionExists() {
        BigDecimal foodPheContent = BigDecimal.valueOf(200);
        BigDecimal oldAmount = BigDecimal.valueOf(25);
        BigDecimal oldPheAmount = BigDecimal.valueOf(5).setScale(4, RoundingMode.HALF_UP);
        BigDecimal newAmount = BigDecimal.valueOf(50);
        BigDecimal newPheAmount = BigDecimal.valueOf(100).setScale(4, RoundingMode.HALF_UP);
        FoodConsumptionRequest request = new FoodConsumptionRequest(newAmount);
        User user = TestEntityFactory.user();
        Food food = TestEntityFactory.food(TestEntityFactory.foodType());
        food.setId(TestEntityFactory.DEFAULT_ID);
        FoodConsumption existingEntity = TestEntityFactory.foodConsumption(
                user,
                food,
                TestEntityFactory.CONSUMED_AT
        );
        existingEntity.setAmount(oldAmount);
        existingEntity.setPhenylalanineAmount(oldPheAmount);
        existingEntity.getFood().setPhenylalanine(foodPheContent);
        when(userService.getCurrentUserId()).thenReturn(user.getUserId());
        when(foodConsumptionRepository.findByIdAndUser_UserId(foodConsumptionId, user.getUserId())).thenReturn(
                Optional.of(existingEntity));
        when(foodConsumptionRepository.save(any(FoodConsumption.class)))
                .thenAnswer(i -> i.getArguments()[0]);
        FoodConsumptionResponse response = foodConsumptionService.update(foodConsumptionId, request);
        ArgumentCaptor<FoodConsumption> captor = ArgumentCaptor.forClass(FoodConsumption.class);
        verify(foodConsumptionRepository).save(captor.capture());
        FoodConsumption savedEntity = captor.getValue();
        Assertions.assertThat(savedEntity.getPhenylalanineAmount()).isEqualByComparingTo(newPheAmount);
        Assertions.assertThat(savedEntity.getAmount()).isEqualByComparingTo(newAmount);
        doAssertionsCheckOnResponse(response, savedEntity);
    }

    @Test
    void update_shouldThrowExceptionAndSaveNothing_whenResourceNotFound() {
        FoodConsumptionRequest request = new FoodConsumptionRequest(BigDecimal.TEN);
        when(userService.getCurrentUserId()).thenReturn(TestEntityFactory.DEFAULT_ID);
        when(foodConsumptionRepository.findByIdAndUser_UserId(foodConsumptionId, TestEntityFactory.DEFAULT_ID))
                .thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> foodConsumptionService.update(foodConsumptionId, request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(foodConsumptionRepository, never()).save(any());
    }

    @Test
    void deleteById_shouldDeleteEntity() {
        User user = TestEntityFactory.user();
        FoodConsumption existingEntity = TestEntityFactory.foodConsumption(
                user,
                TestEntityFactory.food(TestEntityFactory.foodType()),
                TestEntityFactory.CONSUMED_AT
        );
        when(userService.getCurrentUserId()).thenReturn(user.getUserId());
        when(foodConsumptionRepository.findByIdAndUser_UserId(foodConsumptionId, user.getUserId())).thenReturn(
                Optional.of(existingEntity));
        foodConsumptionService.deleteById(foodConsumptionId);
        verify(foodConsumptionRepository).delete(existingEntity);
    }

    @Test
    void deleteById_shouldThrowExceptionAndSaveNothing_whenResourceNotFound() {
        when(userService.getCurrentUserId()).thenReturn(TestEntityFactory.DEFAULT_ID);
        when(foodConsumptionRepository.findByIdAndUser_UserId(foodConsumptionId,
                TestEntityFactory.DEFAULT_ID)).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> foodConsumptionService.deleteById(foodConsumptionId))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(foodConsumptionRepository, never()).delete(any());
    }

    @Test
    void save_shouldNormalizeConsumedAtToUtc() {
        Long foodId = 1L;
        ZonedDateTime cetTime = ZonedDateTime.parse("2026-07-20T01:00:00+02:00");
        FoodConsumptionCreateRequest request = new FoodConsumptionCreateRequest(foodId, BigDecimal.TEN, cetTime);
        User user = TestEntityFactory.user();
        Food food = TestEntityFactory.food(TestEntityFactory.foodType());
        food.setId(foodId);
        when(userService.getCurrentUserId()).thenReturn(TestEntityFactory.DEFAULT_ID);
        when(userService.getCurrentUserReference()).thenReturn(user);
        when(foodReadService.findByIdOrThrow(foodId, TestEntityFactory.DEFAULT_ID)).thenReturn(food);
        when(foodConsumptionRepository.save(any(FoodConsumption.class)))
                .thenAnswer(i -> i.getArguments()[0]);
        FoodConsumptionResponse response = foodConsumptionService.save(request);
        Assertions.assertThat(response.consumedAt()).isEqualTo(ZonedDateTime.parse("2026-07-19T23:00:00Z"));
        Assertions.assertThat(response.consumedAt().getZone()).isEqualTo(ZoneOffset.UTC);
    }

    @ParameterizedTest(name = "{index} - {0}")
    @MethodSource("timezoneDayBoundaryTestCases")
    void findAllByDate_withTimezone_shouldQueryExpectedUtcDayBounds(String scenario,
            LocalDate localDate,
            String timezone,
            ZonedDateTime expectedStartUtc,
            ZonedDateTime expectedEndUtc) {
        Long userId = 1L;
        PaginationRequest paginationRequest = new PaginationRequest(0, 20);
        when(userService.getCurrentUserId()).thenReturn(userId);
        ArgumentCaptor<ZonedDateTime> startCaptor = ArgumentCaptor.forClass(ZonedDateTime.class);
        ArgumentCaptor<ZonedDateTime> endCaptor = ArgumentCaptor.forClass(ZonedDateTime.class);
        when(foodConsumptionRepository.findAllByUserAndConsumedAtBetween(eq(userId), startCaptor.capture(),
                endCaptor.capture(), any(Pageable.class)))
                .thenReturn(Page.empty());
        foodConsumptionService.findAllByDate(localDate.atStartOfDay(ZoneOffset.UTC), timezone, paginationRequest);
        Assertions.assertThat(startCaptor.getValue().toInstant()).isEqualTo(expectedStartUtc.toInstant());
        Assertions.assertThat(endCaptor.getValue().toInstant()).isEqualTo(expectedEndUtc.toInstant());
    }

    private static Stream<Arguments> timezoneDayBoundaryTestCases() {
        return Stream.of(
                Arguments.of("Winter CET (UTC+1)",
                        LocalDate.of(2026, 11, 20),
                        "Europe/Budapest",
                        ZonedDateTime.parse("2026-11-19T23:00:00Z"),
                        ZonedDateTime.parse("2026-11-20T23:00:00Z")),
                Arguments.of("Summer CEST (UTC+2)",
                        LocalDate.of(2026, 7, 20),
                        "Europe/Budapest",
                        ZonedDateTime.parse("2026-07-19T22:00:00Z"),
                        ZonedDateTime.parse("2026-07-20T22:00:00Z")),
                Arguments.of("DST spring forward (23-hour day)",
                        LocalDate.of(2026, 3, 29),
                        "Europe/Budapest",
                        ZonedDateTime.parse("2026-03-28T23:00:00Z"),
                        ZonedDateTime.parse("2026-03-29T22:00:00Z")),
                Arguments.of("DST fall back (25-hour day)",
                        LocalDate.of(2026, 10, 25),
                        "Europe/Budapest",
                        ZonedDateTime.parse("2026-10-24T22:00:00Z"),
                        ZonedDateTime.parse("2026-10-25T23:00:00Z"))
        );
    }

    private void doAssertionsCheckOnResponse(FoodConsumptionResponse response, FoodConsumption foodConsumption) {
        Assertions.assertThat(response.id()).isEqualTo(foodConsumption.getId());
        Assertions.assertThat(response.foodId()).isEqualTo(foodConsumption.getFood().getId());
        Assertions.assertThat(response.amount()).isEqualTo(foodConsumption.getAmount());
        Assertions.assertThat(response.consumedAt()).isNotNull();
        Assertions.assertThat(response.phenylalanineAmount()).isEqualByComparingTo(
                foodConsumption.getPhenylalanineAmount());
    }

}
