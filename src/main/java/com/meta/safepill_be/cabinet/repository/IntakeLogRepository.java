package com.meta.safepill_be.cabinet.repository;

import com.meta.safepill_be.cabinet.domain.IntakeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IntakeLogRepository extends JpaRepository<IntakeLog, Long> {
    List<IntakeLog> findByIntakeSchedule_Id(Long scheduleId);

    Optional<IntakeLog> findByIntakeSchedule_IdAndActualTimeBetween(
            Long scheduleId,
            LocalDateTime start,
            LocalDateTime end);

    @Query("""
            SELECT l
            FROM IntakeLog l
            JOIN FETCH l.intakeSchedule s
            JOIN FETCH s.userMedicationReg r
            WHERE r.user.id = :userId
              AND l.actualTime >= :start
              AND l.actualTime < :end
            ORDER BY l.actualTime ASC
            """)
    List<IntakeLog> findByUserIdAndActualDate(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
