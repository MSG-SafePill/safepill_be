package com.meta.safepill_be.cabinet.service;

import com.meta.safepill_be.cabinet.domain.IntakeSchedule;
import com.meta.safepill_be.cabinet.domain.ItemType;
import com.meta.safepill_be.cabinet.domain.UserMedicationReg;
import com.meta.safepill_be.cabinet.dto.IntakeScheduleResponseDto;
import com.meta.safepill_be.cabinet.dto.OcrRegisterRequestDto;
import com.meta.safepill_be.cabinet.dto.OcrRegisterResponseDto;
import com.meta.safepill_be.cabinet.repository.IntakeScheduleRepository;
import com.meta.safepill_be.cabinet.repository.UserMedicationRegRepository;
import com.meta.safepill_be.medicine.domain.MedicineMaster;
import com.meta.safepill_be.medicine.domain.SupplementMaster;
import com.meta.safepill_be.medicine.repository.MedicineMasterRepository;
import com.meta.safepill_be.medicine.repository.SupplementMasterRepository;
import com.meta.safepill_be.user.domain.User;
import com.meta.safepill_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OcrRegistrationService {
    private final UserRepository userRepository;
    private final UserMedicationRegRepository userMedicationRegRepository;
    private final IntakeScheduleRepository intakeScheduleRepository;
    private final MedicineMasterRepository medicineRepository;
    private final SupplementMasterRepository supplementRepository;

    @Transactional
    public OcrRegisterResponseDto register(String loginId, OcrRegisterRequestDto requestDto) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (requestDto.getItems() == null || requestDto.getItems().isEmpty()) {
            throw new IllegalArgumentException("등록할 항목이 없습니다.");
        }

        List<OcrRegisterResponseDto.ItemResult> results = new ArrayList<>();
        for (OcrRegisterRequestDto.Item item : requestDto.getItems()) {
            results.add(registerItem(user, item));
        }

        return OcrRegisterResponseDto.builder()
                .items(results)
                .build();
    }

    private OcrRegisterResponseDto.ItemResult registerItem(User user, OcrRegisterRequestDto.Item item) {
        if (item.getItemType() == null || item.getItemId() == null) {
            throw new IllegalArgumentException("itemType과 itemId는 필수입니다.");
        }
        validateItemExists(item.getItemType(), item.getItemId());

        UserMedicationReg reg = userMedicationRegRepository.findByUserId(user.getId()).stream()
                .filter(existing -> existing.getItem_type() == item.getItemType() && existing.getItemId().equals(item.getItemId()))
                .findFirst()
                .orElse(null);
        boolean alreadyRegistered = reg != null;

        if (reg == null) {
            reg = new UserMedicationReg();
            reg.setUser(user);
            reg.setItem_type(item.getItemType());
            reg.setItemId(item.getItemId());
            reg = userMedicationRegRepository.save(reg);
        }

        List<IntakeScheduleResponseDto> schedules = createSchedules(reg, item.getSchedules());
        return OcrRegisterResponseDto.ItemResult.builder()
                .regId(reg.getId())
                .itemType(reg.getItem_type())
                .itemId(reg.getItemId())
                .itemName(resolveItemName(reg.getItem_type(), reg.getItemId()))
                .alreadyRegistered(alreadyRegistered)
                .schedules(schedules)
                .build();
    }

    private List<IntakeScheduleResponseDto> createSchedules(UserMedicationReg reg, List<OcrRegisterRequestDto.Schedule> schedules) {
        if (schedules == null || schedules.isEmpty()) {
            return List.of();
        }

        List<IntakeScheduleResponseDto> created = new ArrayList<>();
        for (OcrRegisterRequestDto.Schedule requestSchedule : schedules) {
            String timeSlot = requestSchedule.getTimeSlot();
            if (timeSlot == null || timeSlot.isBlank()) {
                timeSlot = requestSchedule.getTakeTime();
            }
            if (timeSlot == null || timeSlot.isBlank()) {
                continue;
            }
            timeSlot = timeSlot.trim();

            if (intakeScheduleRepository.existsDuplicateSchedule(reg.getId(), timeSlot)) {
                continue;
            }
            IntakeSchedule schedule = new IntakeSchedule();
            schedule.setUserMedicationReg(reg);
            schedule.setTimeSlot(timeSlot);
            created.add(toScheduleResponse(intakeScheduleRepository.save(schedule)));
        }
        return created;
    }

    private void validateItemExists(ItemType itemType, Long itemId) {
        if (itemType == ItemType.MEDICINE && !medicineRepository.existsById(itemId)) {
            throw new IllegalArgumentException("존재하지 않는 약품 ID입니다.");
        }
        if (itemType == ItemType.SUPPLEMENT && !supplementRepository.existsById(itemId)) {
            throw new IllegalArgumentException("존재하지 않는 영양제 ID입니다.");
        }
    }

    private IntakeScheduleResponseDto toScheduleResponse(IntakeSchedule schedule) {
        UserMedicationReg reg = schedule.getUserMedicationReg();
        return IntakeScheduleResponseDto.builder()
                .scheduleId(schedule.getId())
                .regId(reg.getId())
                .itemName(resolveItemName(reg.getItem_type(), reg.getItemId()))
                .timeSlot(schedule.getTimeSlot())
                .takeTime(schedule.getTimeSlot())
                .build();
    }

    private String resolveItemName(ItemType itemType, Long itemId) {
        if (itemType == ItemType.MEDICINE) {
            return medicineRepository.findById(itemId)
                    .map(MedicineMaster::getMedicineName)
                    .orElse("알 수 없는 약품");
        }
        if (itemType == ItemType.SUPPLEMENT) {
            return supplementRepository.findById(itemId)
                    .map(SupplementMaster::getSupplementName)
                    .orElse("알 수 없는 영양제");
        }
        return "알 수 없는 항목";
    }
}
