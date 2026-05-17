package com.meta.safepill_be.medicine.repository;

import com.meta.safepill_be.medicine.domain.MedicineIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicineIngredientRepository extends JpaRepository<MedicineIngredient, Long> {
    List<MedicineIngredient> findByMedicineMaster_Id(Long medicineId);
    boolean existsByMedicineMaster_IdAndIngredientMaster_Id(Long medicineId, Long ingredientId);
}
