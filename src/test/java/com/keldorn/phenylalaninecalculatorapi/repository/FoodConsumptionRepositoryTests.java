package com.keldorn.phenylalaninecalculatorapi.repository;

import com.keldorn.phenylalaninecalculatorapi.annotation.MySQLRepositoryTest;
import com.keldorn.phenylalaninecalculatorapi.domain.entity.Food;
import com.keldorn.phenylalaninecalculatorapi.domain.entity.FoodConsumption;
import com.keldorn.phenylalaninecalculatorapi.domain.entity.FoodType;
import com.keldorn.phenylalaninecalculatorapi.domain.entity.User;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.*;
import java.util.List;

@MySQLRepositoryTest
public class FoodConsumptionRepositoryTests {

    @Autowired
    private FoodConsumptionRepository foodConsumptionRepository;
    @Autowired
    private FoodRepository foodRepository;
    @Autowired
    private FoodTypeRepository foodTypeRepository;
    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    public void setUp() {
        user = userRepository.save(TestEntityFactory.user());

        FoodType foodType = foodTypeRepository.save(TestEntityFactory.foodType());
        Food food = foodRepository.save(TestEntityFactory.food(foodType));
        foodConsumptionRepository.save(TestEntityFactory.foodConsumption(user, food, TestEntityFactory.CONSUMED_AT));
    }

    @Test
    public void findAllByUserAndConsumedAtBetween_shouldReturnListOfFoodConsumption() {
        Pageable pageable = PageRequest.of(0, 20);

        List<FoodConsumption> foodConsumptionResult = foodConsumptionRepository
                .findAllByUserAndConsumedAtBetween(user.getUserId(),
                        TestEntityFactory.START, TestEntityFactory.END, pageable).getContent();

        Assertions.assertThat(foodConsumptionResult).isNotEmpty();
        Assertions.assertThat(foodConsumptionResult)
                .hasSize(1)
                .extracting(FoodConsumption::getFood)
                .extracting(Food::getName)
                .containsExactly(TestEntityFactory.DEFAULT_FOOD_NAME);
    }

    @Test
    public void findAllByUserAndConsumedAtBetween_shouldReturnListOfFoodConsumption_whenInvalidUser() {
        Pageable pageable = PageRequest.of(0, 20);

        List<FoodConsumption> foodConsumptionResult = foodConsumptionRepository
                .findAllByUserAndConsumedAtBetween(Long.MAX_VALUE,
                        TestEntityFactory.START, TestEntityFactory.END, pageable).getContent();

        Assertions.assertThat(foodConsumptionResult).isEmpty();
    }

    @Test
    public void findAllByUserAndConsumedAtBetween_shouldReturnListOfFoodConsumption_whenInvalidDateInterval() {
        Pageable pageable = PageRequest.of(0, 20);

        List<FoodConsumption> foodConsumptionResult = foodConsumptionRepository
                .findAllByUserAndConsumedAtBetween(user.getUserId(),
                        TestEntityFactory.END, TestEntityFactory.END.plusSeconds(60L), pageable).getContent();

        Assertions.assertThat(foodConsumptionResult).isEmpty();
    }
}
