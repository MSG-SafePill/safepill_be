package com.meta.safepill_be.medicine.repository;

import com.meta.safepill_be.medicine.domain.InteractionRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InteractionRuleRepository extends JpaRepository<InteractionRule, Long> {
    @Query("SELECT ir FROM InteractionRule ir " +
            "WHERE ir.ingredientA.id IN :ingredientIds " +
            "AND ir.ingredientB.id IN :ingredientIds")
    List<InteractionRule> findInteractionsByIngredientIds(@Param("ingredientIds") List<Long> ingredientIds);

    List<InteractionRule> findByIngredientA_IdOrIngredientB_Id(Long ingredientIdA, Long ingredientIdB);

    boolean existsByIngredientA_IdAndIngredientB_Id(Long ingredientAId, Long ingredientBId);
}
