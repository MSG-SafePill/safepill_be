package com.meta.safepill_be.cabinet.repository;

import com.meta.safepill_be.cabinet.domain.IntakeSchedule;
import com.meta.safepill_be.cabinet.domain.ScheduleDayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;

public interface IntakeScheduleRepository extends JpaRepository<IntakeSchedule, Long> {
    List<IntakeSchedule> findByUserMedicationReg_Id(Long userMedicationRegId);

    @Query("""
            SELECT COUNT(s) > 0
            FROM IntakeSchedule s
            WHERE s.userMedicationReg.id = :userMedicationRegId
              AND s.takeTime = :takeTime
              AND (
                    s.dayOfWeek = :dayOfWeek
                    OR s.dayOfWeek = com.meta.safepill_be.cabinet.domain.ScheduleDayOfWeek.EVERYDAY
                    OR :dayOfWeek = com.meta.safepill_be.cabinet.domain.ScheduleDayOfWeek.EVERYDAY
                  )
            """)
    boolean existsDuplicateSchedule(
            @Param("userMedicationRegId") Long userMedicationRegId,
            @Param("takeTime") LocalTime takeTime,
            @Param("dayOfWeek") ScheduleDayOfWeek dayOfWeek);

    @Query("""
            SELECT COUNT(s) > 0
            FROM IntakeSchedule s
            WHERE s.userMedicationReg.id = :userMedicationRegId
              AND s.id <> :scheduleId
              AND s.takeTime = :takeTime
              AND (
                    s.dayOfWeek = :dayOfWeek
                    OR s.dayOfWeek = com.meta.safepill_be.cabinet.domain.ScheduleDayOfWeek.EVERYDAY
                    OR :dayOfWeek = com.meta.safepill_be.cabinet.domain.ScheduleDayOfWeek.EVERYDAY
                  )
            """)
    boolean existsDuplicateScheduleExceptId(
            @Param("scheduleId") Long scheduleId,
            @Param("userMedicationRegId") Long userMedicationRegId,
            @Param("takeTime") LocalTime takeTime,
            @Param("dayOfWeek") ScheduleDayOfWeek dayOfWeek);

    @Query("""
            SELECT s
            FROM IntakeSchedule s
            JOIN FETCH s.userMedicationReg r
            WHERE r.user.id = :userId
              AND (s.dayOfWeek = :dayOfWeek OR s.dayOfWeek = com.meta.safepill_be.cabinet.domain.ScheduleDayOfWeek.EVERYDAY)
            ORDER BY s.takeTime ASC
            """)
    List<IntakeSchedule> findSchedulesForUserByDay(
            @Param("userId") Long userId,
            @Param("dayOfWeek") ScheduleDayOfWeek dayOfWeek);
}
