package com.meta.safepill_be.medicine.domain;

import com.meta.safepill_be.common.domain.TimeStamped;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ingredient_master")
public class IngredientMaster extends TimeStamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ingredient_name", nullable = false)
    private String ingredientName;

    @Column(name = "upper_limit", precision = 5, scale = 2)
    private BigDecimal upperLimit;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "best_time_guide", nullable = false)
    private String bestTimeGuide;

    @Column(name = "intake_tip")
    private String intakeTip;
}
