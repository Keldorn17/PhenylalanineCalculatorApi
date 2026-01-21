package com.keldorn.phenylalaninecalculatorapi.repository;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.Food;
import com.keldorn.phenylalaninecalculatorapi.domain.entity.FoodConsumption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface FoodConsumptionRepository extends JpaRepository<FoodConsumption, Long> {

    @Query("FROM FoodConsumption f WHERE f.user.userId = ?1 AND f.consumedAt >= ?2 AND f.consumedAt < ?3")
    List<FoodConsumption> findAllByUserAndConsumedAtBetween(Long userId, Instant start, Instant end);

    FoodConsumption food(Food food);
}
