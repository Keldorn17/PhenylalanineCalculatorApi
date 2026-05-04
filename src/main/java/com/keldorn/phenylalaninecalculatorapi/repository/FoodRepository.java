package com.keldorn.phenylalaninecalculatorapi.repository;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.Food;

import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FoodRepository extends JpaRepository<Food, Long>, JpaSpecificationExecutor<Food> {

    @Modifying
    @Query("UPDATE Food f SET f.user.userId = :updatedUserId WHERE f.user.userId = :userId")
    int updateFoodUser(@Param("userId") Long userId, @Param("updatedUserId") Long updatedUserId);

    @NonNull
    @Override
    @EntityGraph(attributePaths = {"foodType"})
    Optional<Food> findById(@NonNull Long id);

    @NonNull
    @Override
    @EntityGraph(attributePaths = {"foodType"})
    Page<Food> findAll(@NonNull Specification<Food> spec, @NonNull Pageable pageable);

}
