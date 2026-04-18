package com.meta.safepill_be.medicine.repository;

import com.meta.safepill_be.medicine.domain.MedicineMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicineMasterRepository extends JpaRepository<MedicineMaster, Long> {
    Optional<MedicineMaster> findByItemSeq(String itemSeq);
    Page<MedicineMaster> findByMedicineNameContaining(String medicineName, Pageable pageable);
    List<MedicineMaster> findByEfficacyIsNull();
}