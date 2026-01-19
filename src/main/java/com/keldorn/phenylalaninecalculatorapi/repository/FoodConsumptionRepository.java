package com.keldorn.phenylalaninecalculatorapi.repository;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.FoodConsumption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodConsumptionRepository extends JpaRepository<FoodConsumption, Long> {
}
