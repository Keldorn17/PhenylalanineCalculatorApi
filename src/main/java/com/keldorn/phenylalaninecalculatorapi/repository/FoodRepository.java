package com.keldorn.phenylalaninecalculatorapi.repository;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.Food;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FoodRepository extends JpaRepository<Food, Long>, JpaSpecificationExecutor<Food> {

    @Modifying
    @Query("UPDATE Food f SET f.user.userId = :updatedUserId WHERE f.user.userId = :userId")
    int updateFoodUser(@Param("userId") Long userId, @Param("updatedUserId") Long updatedUserId);

    @Query("SELECT DISTINCT f FROM Food f WHERE f.id IN :foodIds")
    List<Food> findAllByIds(@Param("foodIds") List<Long> foodId);

    default Page<Long> findSortedFoodIds(Specification<Food> spec, Pageable pageable) {
        return findBy(spec, query -> query.as(FoodIdProjection.class).page(pageable)
                .map(FoodIdProjection::getId));
    }

    interface FoodIdProjection {

        Long getId();

    }

}
