package com.meta.safepill_be.cabinet.service;

import com.meta.safepill_be.cabinet.domain.IntakeLog;
import com.meta.safepill_be.cabinet.domain.IntakeSchedule;
import com.meta.safepill_be.cabinet.domain.IntakeStatus;
import com.meta.safepill_be.cabinet.domain.ItemType;
import com.meta.safepill_be.cabinet.domain.UserMedicationReg;
import com.meta.safepill_be.cabinet.dto.IntakeLogRequestDto;
import com.meta.safepill_be.cabinet.dto.IntakeLogResponseDto;
import com.meta.safepill_be.cabinet.repository.IntakeLogRepository;
import com.meta.safepill_be.cabinet.repository.IntakeScheduleRepository;
import com.meta.safepill_be.medicine.domain.MedicineMaster;
import com.meta.safepill_be.medicine.domain.SupplementMaster;
import com.meta.safepill_be.medicine.repository.MedicineMasterRepository;
import com.meta.safepill_be.medicine.repository.SupplementMasterRepository;
import com.meta.safepill_be.user.domain.User;
import com.meta.safepill_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IntakeLogService {
    private final UserRepository userRepository;
    private final IntakeScheduleRepository intakeScheduleRepository;
    private final IntakeLogRepository intakeLogRepository;
    private final MedicineMasterRepository medicineRepository;
    private final SupplementMasterRepository supplementRepository;

    @Transactional
    public IntakeLogResponseDto createLog(String loginId, Long scheduleId, IntakeLogRequestDto requestDto) {
        User user = getUser(loginId);
        IntakeSchedule schedule = intakeScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 복약 스케줄입니다."));
        validateOwner(schedule.getUserMedicationReg(), user);

        LocalDateTime actualTime = requestDto.getActualTime() != null ? requestDto.getActualTime() : LocalDateTime.now();
        IntakeStatus status = requestDto.getStatus() != null ? requestDto.getStatus() : IntakeStatus.TAKEN;
        LocalDate targetDate = actualTime.toLocalDate();

        intakeLogRepository.findByIntakeSchedule_IdAndActualTimeBetween(
                scheduleId,
                targetDate.atStartOfDay(),
                targetDate.plusDays(1).atStartOfDay()
        ).ifPresent(existing -> {
            throw new IllegalArgumentException("해당 날짜에는 이미 복약 기록이 존재합니다.");
        });

        IntakeLog log = new IntakeLog();
        log.setIntakeSchedule(schedule);
        log.setStatus(status);
        log.setActualTime(actualTime);
        return toResponseDto(intakeLogRepository.save(log));
    }

    @Transactional
    public IntakeLogResponseDto updateLog(String loginId, Long logId, IntakeLogRequestDto requestDto) {
        User user = getUser(loginId);
        IntakeLog log = intakeLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 복약 기록입니다."));
        validateOwner(log.getIntakeSchedule().getUserMedicationReg(), user);

        if (requestDto.getStatus() != null) {
            log.setStatus(requestDto.getStatus());
        }
        if (requestDto.getActualTime() != null) {
            log.setActualTime(requestDto.getActualTime());
        }
        return toResponseDto(log);
    }

    @Transactional(readOnly = true)
    public List<IntakeLogResponseDto> getLogsByDate(String loginId, LocalDate date) {
        User user = getUser(loginId);
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return intakeLogRepository.findByUserIdAndActualDate(
                        user.getId(),
                        targetDate.atStartOfDay(),
                        targetDate.plusDays(1).atStartOfDay())
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional
    public String deleteLog(String loginId, Long logId) {
        User user = getUser(loginId);
        IntakeLog log = intakeLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 복약 기록입니다."));
        validateOwner(log.getIntakeSchedule().getUserMedicationReg(), user);
        intakeLogRepository.delete(log);
        return "복약 기록이 삭제되었습니다.";
    }

    private User getUser(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private void validateOwner(UserMedicationReg medicationReg, User user) {
        if (!medicationReg.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("해당 복약 기록에 접근할 권한이 없습니다.");
        }
    }

    private IntakeLogResponseDto toResponseDto(IntakeLog log) {
        IntakeSchedule schedule = log.getIntakeSchedule();
        UserMedicationReg reg = schedule.getUserMedicationReg();
        return IntakeLogResponseDto.builder()
                .logId(log.getId())
                .scheduleId(schedule.getId())
                .regId(reg.getId())
                .itemName(resolveItemName(reg))
                .timeSlot(schedule.getTimeSlot())
                .takeTime(schedule.getTimeSlot())
                .status(log.getStatus())
                .actualTime(log.getActualTime())
                .build();
    }

    private String resolveItemName(UserMedicationReg reg) {
        if (reg.getItem_type() == ItemType.MEDICINE) {
            return medicineRepository.findById(reg.getItemId())
                    .map(MedicineMaster::getMedicineName)
                    .orElse("알 수 없는 약품");
        }
        if (reg.getItem_type() == ItemType.SUPPLEMENT) {
            return supplementRepository.findById(reg.getItemId())
                    .map(SupplementMaster::getSupplementName)
                    .orElse("알 수 없는 영양제");
        }
        return "알 수 없는 항목";
    }
}
