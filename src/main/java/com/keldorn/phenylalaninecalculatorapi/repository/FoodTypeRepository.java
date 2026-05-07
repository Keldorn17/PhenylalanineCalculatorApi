package com.keldorn.phenylalaninecalculatorapi.repository;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.FoodType;

import java.util.Optional;

import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FoodTypeRepository extends JpaRepository<FoodType, Long> {

    @Modifying
    @Query(value = "UPDATE food_type SET user_id = :updatedUserId WHERE user_id = :userId", nativeQuery = true)
    int updateFoodTypeUser(@Param("userId") Long userId, @Param("updatedUserId") Long updatedUserId);

    @Override
    @NullMarked
    @Query("FROM FoodType ft WHERE ft.isDeleted = false")
    Page<FoodType> findAll(Pageable pageable);

    @Override
    @NullMarked
    @Query("FROM FoodType ft WHERE ft.id = :id AND ft.isDeleted = false")
    Optional<FoodType> findById(@Param("id") Long id);

}
