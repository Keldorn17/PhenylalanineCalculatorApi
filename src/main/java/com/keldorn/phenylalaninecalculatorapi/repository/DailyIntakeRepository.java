package com.keldorn.phenylalaninecalculatorapi.repository;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.DailyIntake;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyIntakeRepository extends JpaRepository<DailyIntake, Long> {

    @Query("FROM DailyIntake d WHERE d.user.userId = ?1 AND d.date = ?2")
    Optional<DailyIntake> findByUserIdAndDate(Long userid, LocalDate date);
}
