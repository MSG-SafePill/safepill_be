package com.meta.safepill_be.cabinet.service;

import com.meta.safepill_be.cabinet.domain.IntakeSchedule;
import com.meta.safepill_be.cabinet.domain.ItemType;
import com.meta.safepill_be.cabinet.domain.UserMedicationReg;
import com.meta.safepill_be.cabinet.dto.IntakeScheduleRequestDto;
import com.meta.safepill_be.cabinet.dto.IntakeScheduleResponseDto;
import com.meta.safepill_be.cabinet.repository.IntakeScheduleRepository;
import com.meta.safepill_be.cabinet.repository.UserMedicationRegRepository;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class IntakeScheduleService {
    private final UserRepository userRepository;
    private final UserMedicationRegRepository userMedicationRegRepository;
    private final IntakeScheduleRepository intakeScheduleRepository;
    private final MedicineMasterRepository medicineRepository;
    private final SupplementMasterRepository supplementRepository;

    @Transactional
    public IntakeScheduleResponseDto addSchedule(String loginId, Long regId, IntakeScheduleRequestDto requestDto) {
        User user = getUser(loginId);
        UserMedicationReg medicationReg = userMedicationRegRepository.findById(regId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 약장 등록 정보입니다."));
        validateOwner(medicationReg, user);

        String timeSlot = validateTimeSlot(requestDto.resolveTimeSlot());
        if (intakeScheduleRepository.existsDuplicateSchedule(regId, timeSlot)) {
            throw new IllegalArgumentException("이미 동일한 시간대의 복약 스케줄이 존재합니다.");
        }

        IntakeSchedule schedule = new IntakeSchedule();
        schedule.setUserMedicationReg(medicationReg);
        schedule.setTimeSlot(timeSlot);
        return toResponseDto(intakeScheduleRepository.save(schedule));
    }

    @Transactional(readOnly = true)
    public List<IntakeScheduleResponseDto> getSchedules(String loginId) {
        User user = getUser(loginId);
        return intakeScheduleRepository.findSchedulesForUser(user.getId()).stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IntakeScheduleResponseDto> getTodaySchedules(String loginId) {
        return getSchedules(loginId);
    }

    @Transactional
    public IntakeScheduleResponseDto updateSchedule(String loginId, Long scheduleId, IntakeScheduleRequestDto requestDto) {
        User user = getUser(loginId);
        IntakeSchedule schedule = intakeScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 복약 스케줄입니다."));
        validateOwner(schedule.getUserMedicationReg(), user);

        String timeSlot = requestDto.resolveTimeSlot() != null && !requestDto.resolveTimeSlot().isBlank()
                ? validateTimeSlot(requestDto.resolveTimeSlot())
                : schedule.getTimeSlot();

        boolean duplicated = intakeScheduleRepository.existsDuplicateScheduleExceptId(
                scheduleId,
                schedule.getUserMedicationReg().getId(),
                timeSlot);
        if (duplicated) {
            throw new IllegalArgumentException("이미 동일한 시간대의 복약 스케줄이 존재합니다.");
        }

        schedule.setTimeSlot(timeSlot);
        return toResponseDto(schedule);
    }

    @Transactional
    public String deleteSchedule(String loginId, Long scheduleId) {
        User user = getUser(loginId);
        IntakeSchedule schedule = intakeScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 복약 스케줄입니다."));
        validateOwner(schedule.getUserMedicationReg(), user);

        intakeScheduleRepository.delete(schedule);
        return "복약 스케줄이 삭제되었습니다.";
    }

    private User getUser(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private void validateOwner(UserMedicationReg medicationReg, User user) {
        if (!medicationReg.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("해당 약장 데이터에 접근할 권한이 없습니다.");
        }
    }

    private String validateTimeSlot(String timeSlot) {
        if (timeSlot == null || timeSlot.isBlank()) {
            throw new IllegalArgumentException("복용 시간대는 필수입니다.");
        }
        return timeSlot.trim();
    }

    private IntakeScheduleResponseDto toResponseDto(IntakeSchedule schedule) {
        UserMedicationReg reg = schedule.getUserMedicationReg();
        return IntakeScheduleResponseDto.builder()
                .scheduleId(schedule.getId())
                .regId(reg.getId())
                .itemName(resolveItemName(reg))
                .timeSlot(schedule.getTimeSlot())
                .takeTime(schedule.getTimeSlot())
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
