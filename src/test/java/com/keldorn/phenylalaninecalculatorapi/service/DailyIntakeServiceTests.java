package com.keldorn.phenylalaninecalculatorapi.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.keldorn.phenylalaninecalculatorapi.dto.dailyintake.DailyIntakeResponse;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;
import com.keldorn.phenylalaninecalculatorapi.repository.FoodConsumptionRepository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

@ExtendWith(MockitoExtension.class)
class DailyIntakeServiceTests {

    @Mock
    private FoodConsumptionRepository foodConsumptionRepository;

    @Mock
    private UserService userService;

    @Mock
    private ObjectProvider<DailyIntakeService> selfProvider;

    @InjectMocks
    private DailyIntakeService dailyIntakeService;

    @BeforeEach
    void setUp() {
        lenient().when(selfProvider.getObject()).thenReturn(dailyIntakeService);
    }

    private final Long userId = 1L;

    @Test
    void findByDate_shouldReturnsDailyIntakeResponse_whenFoodConsumptionExists() {
        when(userService.getCurrentUserId()).thenReturn(userId);
        when(foodConsumptionRepository.calculateDailyIntake(eq(userId), any(ZonedDateTime.class),
                any(ZonedDateTime.class)))
                .thenReturn(TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE);
        DailyIntakeResponse response = dailyIntakeService.findByDate(TestEntityFactory.TEST_DATE);
        Assertions.assertThat(response.date()).isEqualTo(TestEntityFactory.TEST_DATE);
        Assertions.assertThat(response.totalPhenylalanine()).isEqualByComparingTo(
                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE);
    }

    @Test
    void findByDate_withZonedDateTime_shouldReturnDailyIntakeResponse() {
        when(userService.getCurrentUserId()).thenReturn(userId);
        when(foodConsumptionRepository.calculateDailyIntake(eq(userId), any(ZonedDateTime.class),
                any(ZonedDateTime.class)))
                .thenReturn(BigDecimal.valueOf(15));
        DailyIntakeResponse response = dailyIntakeService.findByDate(TestEntityFactory.TEST_ZONED_DATE_TIME);
        Assertions.assertThat(response.date()).isEqualTo(TestEntityFactory.TEST_DATE);
        Assertions.assertThat(response.totalPhenylalanine()).isEqualByComparingTo(BigDecimal.valueOf(15));
    }

}
