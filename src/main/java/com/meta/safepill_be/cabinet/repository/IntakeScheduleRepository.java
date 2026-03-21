package com.meta.safepill_be.cabinet.repository;

import com.meta.safepill_be.cabinet.domain.IntakeSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IntakeScheduleRepository extends JpaRepository<IntakeSchedule, Long> {
    List<IntakeSchedule> findByUserMedicationReg_Id(Long userMedicationRegId);
}