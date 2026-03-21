package com.meta.safepill_be.cabinet.repository;

import com.meta.safepill_be.cabinet.domain.IntakeLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IntakeLogRepository extends JpaRepository<IntakeLog, Long> {
    List<IntakeLog> findByIntakeSchedule_Id(Long scheduleId);
}