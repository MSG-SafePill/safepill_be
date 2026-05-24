package com.meta.safepill_be.medicine.repository;

import com.meta.safepill_be.medicine.domain.SupplementMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupplementMasterRepository extends JpaRepository<SupplementMaster, Long> {
    Optional<SupplementMaster> findByItemSeq(String itemSeq);
    Page<SupplementMaster> findBySupplementNameContaining(String supplementName, Pageable pageable);
    @EntityGraph(attributePaths = {"ingredients", "ingredients.ingredientMaster"})
    List<SupplementMaster> findByIdIn(List<Long> ids);
}
