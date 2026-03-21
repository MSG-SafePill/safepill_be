package com.meta.safepill_be.medicine.repository;

import com.meta.safepill_be.medicine.domain.SupplementIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplementIngredientRepository extends JpaRepository<SupplementIngredient, Long> {
    List<SupplementIngredient> findBySupplementMaster_Id(Long supplementId);
}