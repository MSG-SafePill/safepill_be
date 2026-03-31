package com.meta.safepill_be.medicine.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "supplement_ingredient")
public class SupplementIngredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplement_id", nullable = false)
    @JsonIgnore
    private SupplementMaster supplementMaster;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private IngredientMaster ingredientMaster;

    @Column(precision = 8, scale = 3)
    private BigDecimal dosage;
}