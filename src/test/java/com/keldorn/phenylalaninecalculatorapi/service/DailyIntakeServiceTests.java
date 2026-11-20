package com.keldorn.phenylalaninecalculatorapi.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.keldorn.phenylalaninecalculatorapi.dto.dailyintake.DailyIntakeResponse;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;
import com.keldorn.phenylalaninecalculatorapi.repository.FoodConsumptionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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
        Assertions.assertThat(response.consumedPhenylalanine()).isEqualByComparingTo(
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
        Assertions.assertThat(response.consumedPhenylalanine()).isEqualByComparingTo(BigDecimal.valueOf(15));
    }

    @ParameterizedTest(name = "{index} - {0}")
    @MethodSource("timezoneDayBoundaryTestCases")
    void findByDate_withTimezone_shouldQueryExpectedUtcDayBounds(String scenario,
            LocalDate localDate,
            String timezone,
            ZonedDateTime expectedStartUtc,
            ZonedDateTime expectedEndUtc) {
        when(userService.getCurrentUserId()).thenReturn(userId);
        ArgumentCaptor<ZonedDateTime> startCaptor = ArgumentCaptor.forClass(ZonedDateTime.class);
        ArgumentCaptor<ZonedDateTime> endCaptor = ArgumentCaptor.forClass(ZonedDateTime.class);
        when(foodConsumptionRepository.calculateDailyIntake(eq(userId), startCaptor.capture(), endCaptor.capture()))
                .thenReturn(BigDecimal.TEN);
        DailyIntakeResponse response = dailyIntakeService.findByDate(localDate.atStartOfDay(ZoneOffset.UTC), timezone);
        Assertions.assertThat(response.date()).isEqualTo(localDate);
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

}
