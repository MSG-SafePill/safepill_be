package com.meta.safepill_be.cabinet.repository;

import com.meta.safepill_be.cabinet.domain.IntakeSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IntakeScheduleRepository extends JpaRepository<IntakeSchedule, Long> {
    List<IntakeSchedule> findByUserMedicationReg_Id(Long userMedicationRegId);

    @Query("""
            SELECT COUNT(s) > 0
            FROM IntakeSchedule s
            WHERE s.userMedicationReg.id = :userMedicationRegId
              AND s.timeSlot = :timeSlot
            """)
    boolean existsDuplicateSchedule(
            @Param("userMedicationRegId") Long userMedicationRegId,
            @Param("timeSlot") String timeSlot);

    @Query("""
            SELECT COUNT(s) > 0
            FROM IntakeSchedule s
            WHERE s.userMedicationReg.id = :userMedicationRegId
              AND s.id <> :scheduleId
              AND s.timeSlot = :timeSlot
            """)
    boolean existsDuplicateScheduleExceptId(
            @Param("scheduleId") Long scheduleId,
            @Param("userMedicationRegId") Long userMedicationRegId,
            @Param("timeSlot") String timeSlot);

    @Query("""
            SELECT s
            FROM IntakeSchedule s
            JOIN FETCH s.userMedicationReg r
            WHERE r.user.id = :userId
            ORDER BY CASE WHEN UPPER(s.timeSlot) = 'ANYTIME' THEN 0 ELSE 1 END, s.timeSlot ASC
            """)
    List<IntakeSchedule> findSchedulesForUser(@Param("userId") Long userId);
}
