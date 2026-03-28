package com.meta.safepill_be.medicine.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ingredient_master")
public class IngredientMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ingredient_name", nullable = false)
    private String ingredientName;

    @Column(name = "upper_limit", precision = 10, scale = 2)
    private BigDecimal upperLimit;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "best_time_guide", nullable = false)
    private String bestTimeGuide;

    @Column(name = "intake_tip")
    private String intakeTip;

    public void updateLimitAndUnit(BigDecimal upperLimit, String unit) {
        this.upperLimit = upperLimit;
        this.unit = unit;
    }
}