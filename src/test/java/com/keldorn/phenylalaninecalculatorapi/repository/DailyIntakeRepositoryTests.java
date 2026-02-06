package com.keldorn.phenylalaninecalculatorapi.repository;

import com.keldorn.phenylalaninecalculatorapi.annotation.MySQLRepositoryTest;
import com.keldorn.phenylalaninecalculatorapi.domain.entity.DailyIntake;
import com.keldorn.phenylalaninecalculatorapi.domain.entity.User;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@MySQLRepositoryTest
public class DailyIntakeRepositoryTests {

    @Autowired
    private DailyIntakeRepository dailyIntakeRepository;
    @Autowired
    private UserRepository userRepository;

    private User user;

    private static final LocalDate TEST_DATE = LocalDate.of(2026, 1, 1);

    @BeforeEach
    public void setUp() {
        user = userRepository.save(TestEntityFactory.user());
    }

    @Test
    public void DailyIntakeRepository_SaveDuplicateUserAndDate_ThrowsDataIntegrityViolation() {
        DailyIntake dailyIntake = TestEntityFactory.dailyIntake(user, TEST_DATE);
        DailyIntake dailyIntake2 = TestEntityFactory.dailyIntake(user, TEST_DATE);

        dailyIntakeRepository.save(dailyIntake);
        Assertions.assertThatThrownBy(() -> dailyIntakeRepository.saveAndFlush(dailyIntake2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void DailyIntakeRepository_FindByUserIdAndDate_ReturnDailyIntake() {
        DailyIntake dailyIntake = DailyIntake.builder()
                .date(TEST_DATE)
                .user(user)
                .totalPhenylalanine(BigDecimal.valueOf(10))
                .build();

        DailyIntake save = dailyIntakeRepository.save(dailyIntake);

        Optional<DailyIntake> response = dailyIntakeRepository.findByUserIdAndDate(save.getUser().getUserId(), TEST_DATE);

        Assertions.assertThat(response).isPresent();
        Assertions.assertThat(response.get().getTotalPhenylalanine())
                .isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    public void DailyIntakeRepository_FindByUserIdAndDate_InvalidDate_ReturnEmptyOptional() {
        DailyIntake dailyIntake = DailyIntake.builder()
                .date(TEST_DATE)
                .user(user)
                .totalPhenylalanine(BigDecimal.valueOf(10))
                .build();

        DailyIntake save = dailyIntakeRepository.save(dailyIntake);

        Optional<DailyIntake> response = dailyIntakeRepository.findByUserIdAndDate(save.getUser().getUserId(), TEST_DATE.plusDays(1));

        Assertions.assertThat(response.isEmpty()).isTrue();
    }

    @Test
    public void DailyIntakeRepository_FindByUserIdAndDate_InvalidUser_ReturnEmptyOptional() {
        DailyIntake dailyIntake = DailyIntake.builder()
                .date(TEST_DATE)
                .user(user)
                .totalPhenylalanine(BigDecimal.valueOf(10))
                .build();

        dailyIntakeRepository.save(dailyIntake);

        Optional<DailyIntake> response = dailyIntakeRepository.findByUserIdAndDate(100L, TEST_DATE);

        Assertions.assertThat(response.isEmpty()).isTrue();
    }
}
