package com.keldorn.phenylalaninecalculatorapi.repository;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.RefreshToken;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser_UserId(Long userId);

    void deleteByUser_UserIdAndExpiryDateBefore(Long userId, ZonedDateTime now);

}
