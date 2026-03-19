package com.keldorn.phenylalaninecalculatorapi.repository;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.FoodConsumption;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FoodConsumptionRepository extends JpaRepository<FoodConsumption, Long> {

    @Query("FROM FoodConsumption f WHERE f.user.userId = ?1 AND f.consumedAt >= ?2 AND f.consumedAt < ?3")
    Page<FoodConsumption> findAllByUserAndConsumedAtBetween(Long userId, Instant start, Instant end, Pageable pageable);

    @Query("FROM FoodConsumption f WHERE f.id = ?1 AND f.user.userId = ?2")
    Optional<FoodConsumption> findByIdAndUserId(Long id, Long userId);
}
