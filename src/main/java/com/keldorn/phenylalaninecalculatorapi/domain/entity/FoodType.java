package com.keldorn.phenylalaninecalculatorapi.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "food_type")
public class FoodType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "food_type_id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "multiplier", nullable = false)
    private int multiplier;
}
