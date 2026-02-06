package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.DailyIntake;
import com.keldorn.phenylalaninecalculatorapi.domain.entity.User;
import com.keldorn.phenylalaninecalculatorapi.dto.dailyintake.DailyIntakeResponse;
import com.keldorn.phenylalaninecalculatorapi.exception.conflict.DailyIntakeCannotBeLowerThanZeroException;
import com.keldorn.phenylalaninecalculatorapi.exception.notfound.DailyIntakeNotFoundException;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;
import com.keldorn.phenylalaninecalculatorapi.mapper.DailyIntakeMapper;
import com.keldorn.phenylalaninecalculatorapi.repository.DailyIntakeRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DailyIntakeServiceTests {

    @Mock
    private DailyIntakeRepository dailyIntakeRepository;
    @Mock
    private UserService userService;
    @Mock
    private DailyIntakeMapper dailyIntakeMapper;

    @InjectMocks
    private DailyIntakeService dailyIntakeService;

    private final LocalDate TEST_DATE = LocalDate.of(2026, 1, 1);

    @Test
    public void DailyIntakeService_FindByDate_ReturnsDailyIntakeResponse() {
        Long userId = 1L;
        DailyIntake dailyIntake = TestEntityFactory.dailyIntake(TestEntityFactory.user(), TEST_DATE);
        DailyIntakeResponse expectedResponse = new DailyIntakeResponse(1L, TEST_DATE, BigDecimal.TEN);

        when(userService.getCurrentUserId()).thenReturn(userId);
        when(dailyIntakeRepository.findByUserIdAndDate(userId, TEST_DATE))
                .thenReturn(Optional.of(dailyIntake));
        when(dailyIntakeMapper.toResponse(dailyIntake)).thenReturn(expectedResponse);

        DailyIntakeResponse actualResponse = dailyIntakeService.findByDate(TEST_DATE);

        Assertions.assertThat(actualResponse).isNotNull();
        Assertions.assertThat(actualResponse.totalPhenylalanine()).isEqualTo(BigDecimal.TEN);
    }

    @Test
    public void DailyIntakeService_FindByDate_ThrowsDailyIntakeNotFoundException() {
        Long userId = 1L;

        when(userService.getCurrentUserId()).thenReturn(userId);
        when(dailyIntakeRepository.findByUserIdAndDate(userId, TEST_DATE))
                .thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> dailyIntakeService.findByDate(TEST_DATE))
                .isInstanceOf(DailyIntakeNotFoundException.class)
                .hasMessageContaining("No daily intake information found");
    }

    @Test
    public void DailyIntakeService_AddAmount_SuccessfulSubtract() {
        Long userId = 1L;
        BigDecimal currentTotal = BigDecimal.TEN;
        BigDecimal amountToSubtract = BigDecimal.valueOf(-5);

        DailyIntake dailyIntake = DailyIntake.builder()
                .totalPhenylalanine(currentTotal)
                .build();

        when(userService.getCurrentUserId()).thenReturn(userId);
        when(dailyIntakeRepository.findByUserIdAndDate(userId, TEST_DATE))
                .thenReturn(Optional.of(dailyIntake));

        dailyIntakeService.addAmount(TEST_DATE, amountToSubtract);
        ArgumentCaptor<DailyIntake> captor = ArgumentCaptor.forClass(DailyIntake.class);
        verify(dailyIntakeRepository).save(captor.capture());

        DailyIntake savedIntake = captor.getValue();

        Assertions.assertThat(savedIntake.getTotalPhenylalanine())
                .isEqualByComparingTo(BigDecimal.valueOf(5));
    }

    @Test
    public void DailyIntakeService_AddAmount_SuccessfulAddition() {
        Long userId = 1L;
        BigDecimal currentTotal = BigDecimal.TEN;
        BigDecimal amountToAdd = BigDecimal.valueOf(5);

        DailyIntake dailyIntake = DailyIntake.builder()
                .totalPhenylalanine(currentTotal)
                .build();

        when(userService.getCurrentUserId()).thenReturn(userId);
        when(dailyIntakeRepository.findByUserIdAndDate(userId, TEST_DATE))
                .thenReturn(Optional.of(dailyIntake));

        dailyIntakeService.addAmount(TEST_DATE, amountToAdd);
        ArgumentCaptor<DailyIntake> captor = ArgumentCaptor.forClass(DailyIntake.class);
        verify(dailyIntakeRepository).save(captor.capture());

        DailyIntake savedIntake = captor.getValue();

        Assertions.assertThat(savedIntake.getTotalPhenylalanine())
                .isEqualByComparingTo(BigDecimal.valueOf(15));
    }

    @Test
    public void DailyIntakeService_AddAmount_ThrowsDailyIntakeCannotBeLowerThanZeroException() {
        Long userId = 1L;
        BigDecimal currentTotal = BigDecimal.TEN;
        BigDecimal amountToSubtract = BigDecimal.valueOf(-20);

        DailyIntake dailyIntake = DailyIntake.builder()
                .totalPhenylalanine(currentTotal)
                .build();

        when(userService.getCurrentUserId()).thenReturn(userId);
        when(dailyIntakeRepository.findByUserIdAndDate(userId, TEST_DATE))
                .thenReturn(Optional.of(dailyIntake));

        Assertions.assertThatThrownBy(() -> dailyIntakeService.addAmount(TEST_DATE, amountToSubtract))
                .isInstanceOf(DailyIntakeCannotBeLowerThanZeroException.class)
                .hasMessageContaining("Daily intake cannot be lower than zero");

        verify(dailyIntakeRepository, never()).save(any());
    }

    @Test
    public void DailyIntakeService_AddAmount_WhenDateDoesNotExist_CreatesNewAndSaves() {
        Long userId = 1L;
        BigDecimal amountToAdd = BigDecimal.TEN;
        User user = TestEntityFactory.user();

        when(userService.getCurrentUserId()).thenReturn(userId);
        when(userService.getCurrentUser()).thenReturn(user);
        when(dailyIntakeRepository.findByUserIdAndDate(userId, TEST_DATE))
                .thenReturn(Optional.empty());

        dailyIntakeService.addAmount(TEST_DATE, amountToAdd);

        verify(dailyIntakeRepository).save(argThat(savedIntake ->
                savedIntake.getTotalPhenylalanine().equals(amountToAdd) &&
                savedIntake.getDate().equals(TEST_DATE) &&
                savedIntake.getUser().equals(user)
        ));
    }
}
