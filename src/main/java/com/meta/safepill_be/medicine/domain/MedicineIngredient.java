package com.meta.safepill_be.medicine.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "medicine_ingredient")
public class MedicineIngredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private MedicineMaster medicineMaster;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private IngredientMaster ingredientMaster;

    @Column(precision = 8, scale = 3)
    private BigDecimal dosage;
}