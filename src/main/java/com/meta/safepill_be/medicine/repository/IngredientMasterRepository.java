package com.meta.safepill_be.medicine.repository;

import com.meta.safepill_be.medicine.domain.IngredientMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IngredientMasterRepository extends JpaRepository<IngredientMaster, Long> {
    Optional<IngredientMaster> findByIngredientName(String ingredientName);
    List<IngredientMaster> findTop5ByIngredientNameContainingIgnoreCase(String ingredientName);
}
