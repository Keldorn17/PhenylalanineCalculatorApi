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
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DailyIntakeServiceTests {

    @Mock
    private DailyIntakeRepository dailyIntakeRepository;
    @Mock
    private UserService userService;
    @Spy
    private DailyIntakeMapper dailyIntakeMapper = Mappers.getMapper(DailyIntakeMapper.class);

    @InjectMocks
    private DailyIntakeService dailyIntakeService;

    private final Long USER_ID = 1L;

    @Test
    public void findByDate_shouldReturnsDailyIntakeResponse_whenDailyIntakeExists() {
        DailyIntake dailyIntake = TestEntityFactory.dailyIntake(TestEntityFactory.user(), TestEntityFactory.TEST_DATE);

        when(userService.getCurrentUserId()).thenReturn(USER_ID);
        when(dailyIntakeRepository.findByUserIdAndDate(USER_ID, TestEntityFactory.TEST_DATE))
                .thenReturn(Optional.of(dailyIntake));

        DailyIntakeResponse response = dailyIntakeService.findByDate(TestEntityFactory.TEST_DATE);

        verify(dailyIntakeMapper).toResponse(dailyIntake);

        Assertions.assertThat(response.id()).isEqualTo(dailyIntake.getId());
        Assertions.assertThat(response.date()).isEqualTo(dailyIntake.getDate());
        Assertions.assertThat(response.totalPhenylalanine()).isEqualByComparingTo(dailyIntake.getTotalPhenylalanine());
    }

    @Test
    public void findByDate_shouldThrowDailyIntakeNotFoundException() {
        when(userService.getCurrentUserId()).thenReturn(USER_ID);
        when(dailyIntakeRepository.findByUserIdAndDate(USER_ID, TestEntityFactory.TEST_DATE))
                .thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> dailyIntakeService.findByDate(TestEntityFactory.TEST_DATE))
                .isInstanceOf(DailyIntakeNotFoundException.class);
    }

    @Test
    public void addAmount_shouldDoSuccessfulSubtract() {
        BigDecimal currentTotal = BigDecimal.TEN;
        BigDecimal amountToSubtract = BigDecimal.valueOf(-5);

        DailyIntake dailyIntake = DailyIntake.builder()
                .totalPhenylalanine(currentTotal)
                .build();

        when(userService.getCurrentUserId()).thenReturn(USER_ID);
        when(dailyIntakeRepository.findByUserIdAndDate(USER_ID, TestEntityFactory.TEST_DATE))
                .thenReturn(Optional.of(dailyIntake));

        dailyIntakeService.addAmount(TestEntityFactory.TEST_DATE, amountToSubtract);
        ArgumentCaptor<DailyIntake> captor = ArgumentCaptor.forClass(DailyIntake.class);
        verify(dailyIntakeRepository).save(captor.capture());

        DailyIntake savedIntake = captor.getValue();

        int expectedValue = 5;
        Assertions.assertThat(savedIntake.getTotalPhenylalanine())
                .isEqualByComparingTo(BigDecimal.valueOf(expectedValue));
    }

    @Test
    public void addAmount_shouldDoSuccessfulAddition() {
        BigDecimal currentTotal = BigDecimal.TEN;
        BigDecimal amountToAdd = BigDecimal.valueOf(5);

        DailyIntake dailyIntake = DailyIntake.builder()
                .totalPhenylalanine(currentTotal)
                .build();

        when(userService.getCurrentUserId()).thenReturn(USER_ID);
        when(dailyIntakeRepository.findByUserIdAndDate(USER_ID, TestEntityFactory.TEST_DATE))
                .thenReturn(Optional.of(dailyIntake));

        dailyIntakeService.addAmount(TestEntityFactory.TEST_DATE, amountToAdd);
        ArgumentCaptor<DailyIntake> captor = ArgumentCaptor.forClass(DailyIntake.class);
        verify(dailyIntakeRepository).save(captor.capture());

        DailyIntake savedIntake = captor.getValue();

        int expectedValue = 15;
        Assertions.assertThat(savedIntake.getTotalPhenylalanine())
                .isEqualByComparingTo(BigDecimal.valueOf(expectedValue));
    }

    @Test
    public void addAmount_shouldThrowDailyIntakeCannotBeLowerThanZeroException() {
        BigDecimal currentTotal = BigDecimal.TEN;
        BigDecimal amountToSubtract = BigDecimal.valueOf(-20);

        DailyIntake dailyIntake = DailyIntake.builder()
                .totalPhenylalanine(currentTotal)
                .build();

        when(userService.getCurrentUserId()).thenReturn(USER_ID);
        when(dailyIntakeRepository.findByUserIdAndDate(USER_ID, TestEntityFactory.TEST_DATE))
                .thenReturn(Optional.of(dailyIntake));

        Assertions.assertThatThrownBy(() -> dailyIntakeService.addAmount(TestEntityFactory.TEST_DATE, amountToSubtract))
                .isInstanceOf(DailyIntakeCannotBeLowerThanZeroException.class);

        verify(dailyIntakeRepository, never()).save(any());
    }

    @Test
    public void addAmount_shouldCreateNewDailyIntake_whenOneDoesntExists() {
        BigDecimal amountToAdd = BigDecimal.TEN;
        User user = TestEntityFactory.user();

        when(userService.getCurrentUserId()).thenReturn(USER_ID);
        when(userService.getCurrentUser()).thenReturn(user);
        when(dailyIntakeRepository.findByUserIdAndDate(USER_ID, TestEntityFactory.TEST_DATE))
                .thenReturn(Optional.empty());

        dailyIntakeService.addAmount(TestEntityFactory.TEST_DATE, amountToAdd);

        ArgumentCaptor<DailyIntake> captor = ArgumentCaptor.forClass(DailyIntake.class);
        verify(dailyIntakeRepository).save(captor.capture());

        DailyIntake response = captor.getValue();

        Assertions.assertThat(response.getId()).isNull();
        Assertions.assertThat(response.getUser()).isEqualTo(user);
        Assertions.assertThat(response.getDate()).isEqualTo(TestEntityFactory.TEST_DATE);
        Assertions.assertThat(response.getTotalPhenylalanine()).isEqualByComparingTo(amountToAdd);
    }
}
