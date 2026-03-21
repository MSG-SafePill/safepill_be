package com.meta.safepill_be.medicine.repository;

import com.meta.safepill_be.medicine.domain.SupplementMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SupplementMasterRepository extends JpaRepository<SupplementMaster, Long> {
    Optional<SupplementMaster> findByPrdlstReportNo(String prdlstReportNo);
}