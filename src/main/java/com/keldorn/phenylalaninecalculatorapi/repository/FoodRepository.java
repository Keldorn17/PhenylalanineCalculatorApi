package com.keldorn.phenylalaninecalculatorapi.repository;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.Food;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface FoodRepository extends JpaRepository<Food, Long> {

    @Modifying
    @Query("UPDATE Food f SET f.user.userId = ?2 WHERE f.user.userId = ?1")
    void updateFoodUser(Long userId, Long updatedUserId);
}
