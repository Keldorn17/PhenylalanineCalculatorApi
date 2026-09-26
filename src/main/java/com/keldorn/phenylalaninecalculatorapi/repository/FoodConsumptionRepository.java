package com.keldorn.phenylalaninecalculatorapi.repository;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.FoodConsumption;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FoodConsumptionRepository extends JpaRepository<FoodConsumption, Long> {

    @EntityGraph(attributePaths = {"food"})
    @Query("FROM FoodConsumption fc WHERE fc.user.userId = :userId AND fc.consumedAt >= :start AND fc.consumedAt < " +
            ":end")
    Page<FoodConsumption> findAllByUserAndConsumedAtBetween(@Param("userId") Long userId,
            @Param("start") ZonedDateTime start,
            @Param("end") ZonedDateTime end,
            Pageable pageable);

    @EntityGraph(attributePaths = {"food", "user"})
    Optional<FoodConsumption> findByIdAndUser_UserId(Long id, Long userId);

    @Modifying
    @Query("DELETE FROM FoodConsumption fc WHERE fc.user.userId = ?1")
    int deleteFoodConsumptionByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(fc.phenylalanineAmount), 0) FROM FoodConsumption fc WHERE fc.user.userId = :userId " +
            "AND fc.consumedAt >= :startOfDay AND fc.consumedAt < :endOfDay")
    BigDecimal calculateDailyIntake(@Param("userId") Long userId,
            @Param("startOfDay") ZonedDateTime startOfDay,
            @Param("endOfDay") ZonedDateTime endOfDay);

    @Query("SELECT COUNT(fc) > 0 FROM FoodConsumption fc WHERE fc.user.userId = :userId AND fc.consumedAt >= " +
            ":startOfDay AND fc.consumedAt < :endOfDay")
    boolean existsDailyIntake(@Param("userId") Long userId,
            @Param("startOfDay") ZonedDateTime startOfDay,
            @Param("endOfDay") ZonedDateTime endOfDay);

}
