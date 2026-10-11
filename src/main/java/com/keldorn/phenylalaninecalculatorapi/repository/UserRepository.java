package com.keldorn.phenylalaninecalculatorapi.repository;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.User;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameOrEmail(String username, String email);

    @Query("SELECT u.dailyLimit FROM User u WHERE u.userId = :userId")
    Optional<BigDecimal> findDailyLimitByUserId(@Param("userId") Long userId);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

}
