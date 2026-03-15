package com.meta.safepill_be.medicine.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "interaction_rule")
public class InteractionRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "risk_level")
    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_a_id", nullable = false)
    private IngredientMaster ingredientA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_b_id", nullable = false)
    private IngredientMaster ingredientB;
}
