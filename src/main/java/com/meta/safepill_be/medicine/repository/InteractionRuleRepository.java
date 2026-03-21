package com.meta.safepill_be.medicine.repository;

import com.meta.safepill_be.medicine.domain.InteractionRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InteractionRuleRepository extends JpaRepository<InteractionRule, Long> {
    List<InteractionRule> findByIngredientA_IdOrIngredientB_Id(Long ingredientIdA, Long ingredientIdB);
}