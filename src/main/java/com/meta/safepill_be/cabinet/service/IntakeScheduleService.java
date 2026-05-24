package com.meta.safepill_be.cabinet.service;

import com.meta.safepill_be.cabinet.domain.IntakeSchedule;
import com.meta.safepill_be.cabinet.domain.ItemType;
import com.meta.safepill_be.cabinet.domain.ScheduleDayOfWeek;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
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
    public List<IntakeScheduleResponseDto> addSchedule(String loginId, Long regId, IntakeScheduleRequestDto requestDto) {
        User user = getUser(loginId);
        UserMedicationReg medicationReg = userMedicationRegRepository.findById(regId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 약장 등록 정보입니다."));
        validateOwner(medicationReg, user);

        LocalTime takeTime = parseTakeTime(requestDto.getTakeTime());
        String dosage = validateDosage(requestDto.getDosage());
        List<ScheduleDayOfWeek> daysOfWeek = parseDaysOfWeek(requestDto.getDaysOfWeek());
        if (daysOfWeek.contains(ScheduleDayOfWeek.EVERYDAY) && daysOfWeek.size() > 1) {
            throw new IllegalArgumentException("EVERYDAY는 다른 요일과 함께 등록할 수 없습니다.");
        }

        List<IntakeSchedule> schedules = new ArrayList<>();
        for (ScheduleDayOfWeek dayOfWeek : daysOfWeek) {
            boolean duplicated = intakeScheduleRepository.existsDuplicateSchedule(
                    regId, takeTime, dayOfWeek);
            if (duplicated) {
                throw new IllegalArgumentException("이미 동일한 시간과 요일의 복약 스케줄이 존재합니다.");
            }

            IntakeSchedule schedule = new IntakeSchedule();
            schedule.setUserMedicationReg(medicationReg);
            schedule.setTakeTime(takeTime);
            schedule.setDayOfWeek(dayOfWeek);
            schedule.setDosage(dosage);
            schedules.add(schedule);
        }

        return intakeScheduleRepository.saveAll(schedules).stream()
                .sorted(Comparator.comparing(IntakeSchedule::getTakeTime))
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IntakeScheduleResponseDto> getSchedulesByDay(String loginId, ScheduleDayOfWeek requestedDay) {
        User user = getUser(loginId);
        ScheduleDayOfWeek dayOfWeek = requestedDay == null
                ? ScheduleDayOfWeek.valueOf(LocalDate.now().getDayOfWeek().name())
                : requestedDay;

        return intakeScheduleRepository.findSchedulesForUserByDay(user.getId(), dayOfWeek).stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IntakeScheduleResponseDto> getTodaySchedules(String loginId) {
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        return getSchedulesByDay(loginId, ScheduleDayOfWeek.valueOf(today.name()));
    }

    @Transactional
    public IntakeScheduleResponseDto updateSchedule(String loginId, Long scheduleId, IntakeScheduleRequestDto requestDto) {
        User user = getUser(loginId);
        IntakeSchedule schedule = intakeScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 복약 스케줄입니다."));
        validateOwner(schedule.getUserMedicationReg(), user);

        LocalTime takeTime = requestDto.getTakeTime() != null && !requestDto.getTakeTime().isBlank()
                ? parseTakeTime(requestDto.getTakeTime())
                : schedule.getTakeTime();
        String dosage = requestDto.getDosage() != null && !requestDto.getDosage().isBlank()
                ? validateDosage(requestDto.getDosage())
                : schedule.getDosage();
        ScheduleDayOfWeek dayOfWeek = schedule.getDayOfWeek();
        if (requestDto.getDaysOfWeek() != null && !requestDto.getDaysOfWeek().isEmpty()) {
            List<ScheduleDayOfWeek> daysOfWeek = parseDaysOfWeek(requestDto.getDaysOfWeek());
            if (daysOfWeek.size() > 1) {
                throw new IllegalArgumentException("스케줄 수정은 하나의 요일만 지정할 수 있습니다.");
            }
            dayOfWeek = daysOfWeek.get(0);
        }

        boolean duplicated = intakeScheduleRepository.existsDuplicateScheduleExceptId(
                scheduleId,
                schedule.getUserMedicationReg().getId(),
                takeTime,
                dayOfWeek);
        if (duplicated) {
            throw new IllegalArgumentException("이미 동일한 시간과 요일의 복약 스케줄이 존재합니다.");
        }

        schedule.setTakeTime(takeTime);
        schedule.setDayOfWeek(dayOfWeek);
        schedule.setDosage(dosage);
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

    private LocalTime parseTakeTime(String takeTime) {
        if (takeTime == null || takeTime.isBlank()) {
            throw new IllegalArgumentException("복용 시간은 필수입니다.");
        }
        try {
            return LocalTime.parse(takeTime);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("복용 시간은 HH:mm 형식이어야 합니다.");
        }
    }

    private String validateDosage(String dosage) {
        if (dosage == null || dosage.isBlank()) {
            throw new IllegalArgumentException("복용량은 필수입니다.");
        }
        return dosage.trim();
    }

    private List<ScheduleDayOfWeek> parseDaysOfWeek(List<String> daysOfWeek) {
        if (daysOfWeek == null || daysOfWeek.isEmpty()) {
            throw new IllegalArgumentException("복용 요일은 하나 이상 선택해야 합니다.");
        }

        return daysOfWeek.stream()
                .map(day -> {
                    try {
                        return ScheduleDayOfWeek.valueOf(day.trim().toUpperCase());
                    } catch (RuntimeException e) {
                        throw new IllegalArgumentException("복용 요일 값이 올바르지 않습니다: " + day);
                    }
                })
                .distinct()
                .toList();
    }

    private IntakeScheduleResponseDto toResponseDto(IntakeSchedule schedule) {
        UserMedicationReg reg = schedule.getUserMedicationReg();
        return IntakeScheduleResponseDto.builder()
                .scheduleId(schedule.getId())
                .regId(reg.getId())
                .itemName(resolveItemName(reg))
                .takeTime(schedule.getTakeTime())
                .dayOfWeek(schedule.getDayOfWeek())
                .dosage(schedule.getDosage())
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
