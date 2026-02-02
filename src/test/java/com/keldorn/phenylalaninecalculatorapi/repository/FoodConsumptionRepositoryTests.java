package com.keldorn.phenylalaninecalculatorapi.repository;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.Food;
import com.keldorn.phenylalaninecalculatorapi.domain.entity.FoodConsumption;
import com.keldorn.phenylalaninecalculatorapi.domain.entity.FoodType;
import com.keldorn.phenylalaninecalculatorapi.domain.entity.User;
import com.keldorn.phenylalaninecalculatorapi.domain.enums.Role;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class FoodConsumptionRepositoryTests {

    @Autowired private FoodConsumptionRepository foodConsumptionRepository;
    @Autowired private FoodRepository foodRepository;
    @Autowired private FoodTypeRepository foodTypeRepository;
    @Autowired private UserRepository userRepository;

    private User user;

    private final Instant START = ZonedDateTime.of(LocalDate.of(2026, 1, 1),
            LocalTime.of(0, 0), ZoneId.of("UTC")).toInstant();
    private final Instant END = ZonedDateTime.of(LocalDate.of(2026, 1, 2),
            LocalTime.of(0, 0), ZoneId.of("UTC")).toInstant();
    private final Instant CONSUMED_AT = ZonedDateTime.of(LocalDate.of(2026, 1, 1),
            LocalTime.of(12, 0), ZoneId.of("UTC")).toInstant();

    @BeforeEach
    public void setUp() {
        User userSetUp = User.builder()
                .timezone("UTC")
                .dailyLimit(BigDecimal.valueOf(310))
                .email("test@testmail.com")
                .role(Role.ROLE_USER)
                .password("testPassword")
                .username("testUser")
                .build();
        user = userRepository.save(userSetUp);

        FoodType foodType = FoodType.builder()
                .name("GeneralFoodType")
                .multiplier(50)
                .build();
        FoodType savedFoodType = foodTypeRepository.save(foodType);

        Food food = Food.builder()
                .name("TestFood")
                .protein(BigDecimal.TEN)
                .phenylalanine(BigDecimal.TEN)
                .calories(BigDecimal.TEN)
                .foodType(savedFoodType)
                .build();
        Food savedFood = foodRepository.save(food);

        FoodConsumption foodConsumption = FoodConsumption.builder()
                .user(user)
                .food(savedFood)
                .amount(BigDecimal.TEN)
                .phenylalanineAmount(BigDecimal.TEN)
                .consumedAt(CONSUMED_AT)
                .build();
        foodConsumptionRepository.save(foodConsumption);
    }

    @Test
    public void FoodConsumptionRepository_FindAllByUserAndConsumedAtBetween_ReturnListOfFoodConsumption() {
        Pageable pageable = PageRequest.of(0, 20);

        List<FoodConsumption> foodConsumptionResult = foodConsumptionRepository
                .findAllByUserAndConsumedAtBetween(user.getUserId(), START, END, pageable).getContent();

        Assertions.assertThat(foodConsumptionResult).isNotEmpty();
        Assertions.assertThat(foodConsumptionResult)
                .hasSize(1)
                .extracting(FoodConsumption::getFood)
                .extracting(Food::getName)
                .containsExactly("TestFood");
    }

    @Test
    public void FoodConsumptionRepository_FindAllByUserAndConsumedAtBetween_InvalidUser_ReturnListOfFoodConsumption() {
        Pageable pageable = PageRequest.of(0, 20);

        List<FoodConsumption> foodConsumptionResult = foodConsumptionRepository
                .findAllByUserAndConsumedAtBetween(Long.MAX_VALUE, START, END, pageable).getContent();

        Assertions.assertThat(foodConsumptionResult).isEmpty();
    }

    @Test
    public void FoodConsumptionRepository_FindAllByUserAndConsumedAtBetween_InvalidDateInterval_ReturnListOfFoodConsumption() {
        Pageable pageable = PageRequest.of(0, 20);

        List<FoodConsumption> foodConsumptionResult = foodConsumptionRepository
                .findAllByUserAndConsumedAtBetween(user.getUserId(), END, END.plusSeconds(60L), pageable).getContent();

        Assertions.assertThat(foodConsumptionResult).isEmpty();
    }
}
