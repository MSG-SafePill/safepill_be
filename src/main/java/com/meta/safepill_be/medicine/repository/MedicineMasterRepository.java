package com.meta.safepill_be.medicine.repository;

import com.meta.safepill_be.medicine.domain.MedicineMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface MedicineMasterRepository extends JpaRepository<MedicineMaster, Long> {
    Optional<MedicineMaster> findByItemSeq(String itemSeq);
    Page<MedicineMaster> findByMedicineNameContaining(String medicineName, Pageable pageable);
    @EntityGraph(attributePaths = {"ingredients", "ingredients.ingredientMaster"})
    List<MedicineMaster> findTop5ByMedicineNameContainingIgnoreCase(String medicineName);
    @EntityGraph(attributePaths = {"ingredients", "ingredients.ingredientMaster"})
    List<MedicineMaster> findByIdIn(List<Long> ids);
    List<MedicineMaster> findByEfficacyIsNull();
}
