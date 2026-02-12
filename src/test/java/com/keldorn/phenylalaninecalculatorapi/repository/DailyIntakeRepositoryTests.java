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
import java.util.Optional;

@MySQLRepositoryTest
public class DailyIntakeRepositoryTests {

    @Autowired
    private DailyIntakeRepository dailyIntakeRepository;
    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    public void setUp() {
        user = userRepository.save(TestEntityFactory.user());
    }

    @Test
    public void save_shouldThrowDataIntegrityViolation_whenSavingDuplicateUserAndDate() {
        DailyIntake dailyIntake = TestEntityFactory.dailyIntake(user, TestEntityFactory.TEST_DATE);
        DailyIntake dailyIntake2 = TestEntityFactory.dailyIntake(user, TestEntityFactory.TEST_DATE);

        dailyIntakeRepository.save(dailyIntake);
        Assertions.assertThatThrownBy(() -> dailyIntakeRepository.saveAndFlush(dailyIntake2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void findByUserIdAndDate_shouldReturnDailyIntake() {
        DailyIntake dailyIntake = DailyIntake.builder()
                .date(TestEntityFactory.TEST_DATE)
                .user(user)
                .totalPhenylalanine(BigDecimal.valueOf(10))
                .build();

        DailyIntake save = dailyIntakeRepository.save(dailyIntake);

        Optional<DailyIntake> response = dailyIntakeRepository.findByUserIdAndDate(save.getUser().getUserId(), TestEntityFactory.TEST_DATE);

        Assertions.assertThat(response).isPresent();
        Assertions.assertThat(response.get().getTotalPhenylalanine())
                .isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    public void findByUserIdAndDate_shouldReturnEmptyOptional_whenInvalidDate() {
        DailyIntake dailyIntake = DailyIntake.builder()
                .date(TestEntityFactory.TEST_DATE)
                .user(user)
                .totalPhenylalanine(BigDecimal.valueOf(10))
                .build();

        DailyIntake save = dailyIntakeRepository.save(dailyIntake);

        Optional<DailyIntake> response = dailyIntakeRepository.findByUserIdAndDate(save.getUser().getUserId(), TestEntityFactory.TEST_DATE.plusDays(1));

        Assertions.assertThat(response.isEmpty()).isTrue();
    }

    @Test
    public void findByUserIdAndDate_shouldReturnEmptyOptional_whenInvalidUser() {
        DailyIntake dailyIntake = DailyIntake.builder()
                .date(TestEntityFactory.TEST_DATE)
                .user(user)
                .totalPhenylalanine(BigDecimal.valueOf(10))
                .build();

        dailyIntakeRepository.save(dailyIntake);

        Optional<DailyIntake> response = dailyIntakeRepository.findByUserIdAndDate(100L, TestEntityFactory.TEST_DATE);

        Assertions.assertThat(response.isEmpty()).isTrue();
    }
}
