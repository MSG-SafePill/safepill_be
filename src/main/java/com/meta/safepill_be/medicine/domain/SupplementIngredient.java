package com.meta.safepill_be.medicine.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "supplement_ingredient")
public class SupplementIngredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplement_id", nullable = false)
    private SupplementMaster supplementMaster;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private IngredientMaster ingredientMaster;

    @Column(precision = 8, scale = 3)
    private BigDecimal dosage;
}
