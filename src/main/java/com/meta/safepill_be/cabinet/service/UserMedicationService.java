package com.meta.safepill_be.cabinet.service;

import com.meta.safepill_be.cabinet.domain.ItemType;
import com.meta.safepill_be.cabinet.domain.UserMedicationReg;
import com.meta.safepill_be.cabinet.dto.MedicationRegRequestDto;
import com.meta.safepill_be.cabinet.repository.UserMedicationRegRepository;
import com.meta.safepill_be.medicine.repository.MedicineMasterRepository;
import com.meta.safepill_be.medicine.repository.SupplementMasterRepository;
import com.meta.safepill_be.user.domain.User;
import com.meta.safepill_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserMedicationService {
    private final UserRepository userRepository;
    private final MedicineMasterRepository medicineRepository;
    private final SupplementMasterRepository supplementRepository;
    private final UserMedicationRegRepository userMedicationRegRepository;

    @Transactional
    public String addMedication(String loginId, MedicationRegRequestDto requestDto) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        UserMedicationReg medicationReg = new UserMedicationReg();
        medicationReg.setUser(user);
        medicationReg.setItem_type(ItemType.valueOf(requestDto.getType().toUpperCase()));
        medicationReg.setItemId(requestDto.getItemId());
        if (ItemType.MEDICINE.name().equalsIgnoreCase(requestDto.getType())) {
            if (!medicineRepository.existsById(requestDto.getItemId())) {
                throw new IllegalArgumentException("존재하지 않는 약품 ID입니다.");
            }
            medicationReg.setItem_type(ItemType.MEDICINE);
        } else if (ItemType.SUPPLEMENT.name().equalsIgnoreCase(requestDto.getType())) {
            if (!supplementRepository.existsById(requestDto.getItemId())) {
                throw new IllegalArgumentException("존재하지 않는 영양제 ID입니다.");
            }
            medicationReg.setItem_type(ItemType.SUPPLEMENT);
        } else {
            throw new IllegalArgumentException("타입은 MEDICINE 또는 SUPPLEMENT 여야 합니다.");
        }
        medicationReg.setItemId(requestDto.getItemId());
        userMedicationRegRepository.save(medicationReg);
        return "내 약장에 성공적으로 등록되었습니다!";
    }
}