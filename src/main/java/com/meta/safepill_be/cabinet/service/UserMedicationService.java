package com.meta.safepill_be.cabinet.service;

import com.meta.safepill_be.cabinet.domain.ItemType;
import com.meta.safepill_be.cabinet.domain.UserMedicationReg;
import com.meta.safepill_be.cabinet.dto.CabinetIngredientResponseDto;
import com.meta.safepill_be.cabinet.dto.CabinetItemResponseDto;
import com.meta.safepill_be.cabinet.dto.MedicationRegRequestDto;
import com.meta.safepill_be.cabinet.dto.MedicationPreviewDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.meta.safepill_be.cabinet.repository.UserMedicationRegRepository;
import com.meta.safepill_be.medicine.domain.MedicineIngredient;
import com.meta.safepill_be.medicine.domain.MedicineMaster;
import com.meta.safepill_be.medicine.domain.SupplementIngredient;
import com.meta.safepill_be.medicine.domain.SupplementMaster;
import com.meta.safepill_be.medicine.guide.IngredientGuideService;
import com.meta.safepill_be.medicine.repository.MedicineMasterRepository;
import com.meta.safepill_be.medicine.repository.SupplementMasterRepository;
import com.meta.safepill_be.user.domain.User;
import com.meta.safepill_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserMedicationService {
    private final UserRepository userRepository;
    private final MedicineMasterRepository medicineRepository;
    private final SupplementMasterRepository supplementRepository;
    private final UserMedicationRegRepository userMedicationRegRepository;
    private final IngredientGuideService ingredientGuideService;

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
        medicationReg.setSupplyDays(normalizeSupplyDays(requestDto.getSupplyDays()));
        ItemType selectedType = ItemType.valueOf(requestDto.getType().toUpperCase());
        boolean isDuplicate = userMedicationRegRepository.isAlreadyRegistered(user, selectedType, requestDto.getItemId());
        if (isDuplicate) {
            throw new IllegalArgumentException("이미 내 약장에 등록된 약품/영양제입니다.");
        }
        medicationReg.setItem_type(selectedType);
        medicationReg.setItemId(requestDto.getItemId());
        userMedicationRegRepository.save(medicationReg);
        return "내 약장에 성공적으로 등록되었습니다!";
    }

    @Transactional(readOnly = true)
    public List<CabinetItemResponseDto> getMyMedications(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        List<UserMedicationReg> myRegList = userMedicationRegRepository.findByUserId(user.getId());
        List<CabinetItemResponseDto> items = myRegList.stream().map(reg -> {
            if (reg.getItem_type() == ItemType.MEDICINE) {
                MedicineMaster medicine = medicineRepository.findById(reg.getItemId()).orElse(null);
                return CabinetItemResponseDto.builder()
                        .regId(reg.getId())
                        .type(reg.getItem_type())
                        .itemId(reg.getItemId())
                        .supplyDays(reg.getSupplyDays())
                        .itemName(medicine != null ? medicine.getMedicineName() : "알 수 없는 약품")
                        .manufacturer(medicine != null ? medicine.getMedicineManufacturer() : null)
                        .imageUrl(medicine != null && medicine.getAppearanceInfo() != null ? medicine.getAppearanceInfo().getImageUrl() : null)
                        .efficacy(medicine != null ? medicine.getEfficacy() : null)
                        .precautions(medicine != null ? medicine.getPrecautions() : null)
                        .ingredientGuide(ingredientGuideService.assess(medicine == null ? List.of()
                                : medicine.getIngredients().stream()
                                .map(i -> i.getIngredientMaster().getIngredientName()).toList()))
                        .ingredients(medicine != null ? medicine.getIngredients().stream()
                                .map(this::toMedicineIngredientDto)
                                .collect(Collectors.toList()) : List.of())
                        .build();
            } else if (reg.getItem_type() == ItemType.SUPPLEMENT) {
                SupplementMaster supplement = supplementRepository.findById(reg.getItemId()).orElse(null);
                return CabinetItemResponseDto.builder()
                        .regId(reg.getId())
                        .type(reg.getItem_type())
                        .itemId(reg.getItemId())
                        .supplyDays(reg.getSupplyDays())
                        .itemName(supplement != null ? supplement.getSupplementName() : "알 수 없는 영양제")
                        .manufacturer(supplement != null ? supplement.getSupplementManufacturer() : null)
                        .imageUrl(supplement != null && supplement.getAppearanceInfo() != null ? supplement.getAppearanceInfo().getImageUrl() : null)
                        .efficacy(supplement != null ? supplement.getEfficacy() : null)
                        .precautions(supplement != null ? supplement.getPrecautions() : null)
                        .ingredientGuide(ingredientGuideService.assess(supplement == null ? List.of()
                                : supplement.getIngredients().stream()
                                .map(i -> i.getIngredientMaster().getIngredientName()).toList()))
                        .ingredients(supplement != null ? supplement.getIngredients().stream()
                                .map(this::toSupplementIngredientDto)
                                .collect(Collectors.toList()) : List.of())
                        .build();
            }
            return CabinetItemResponseDto.builder()
                    .regId(reg.getId())
                    .type(reg.getItem_type())
                    .itemId(reg.getItemId())
                    .supplyDays(reg.getSupplyDays())
                    .itemName("알 수 없는 항목")
                    .ingredientGuide(ingredientGuideService.assess(List.of()))
                    .ingredients(List.of())
                    .build();
        }).collect(Collectors.toList());
        return CabinetIngredientOverlaps.enrich(items);
    }

    private Integer normalizeSupplyDays(Integer supplyDays) {
        if (supplyDays == null) {
            return null;
        }
        if (supplyDays < 1) {
            throw new IllegalArgumentException("처방/구매 일수는 1일 이상이어야 합니다.");
        }
        return supplyDays;
    }

    @Transactional(readOnly = true)
    public MedicationPreviewDto previewMedication(String loginId, ItemType type, Long itemId) {
        if (itemId == null || itemId <= 0 || type == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바른 제품을 선택해주세요.");
        }
        // Only this authenticated user's cabinet is used. No registration is persisted.
        var cabinet = getMyMedications(loginId);
        String name;
        List<String> names;
        if (type == ItemType.MEDICINE) {
            var medicine = medicineRepository.findById(itemId).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "약품을 찾을 수 없습니다."));
            name = medicine.getMedicineName();
            names = medicine.getIngredients().stream().map(i -> i.getIngredientMaster().getIngredientName()).toList();
        } else {
            var supplement = supplementRepository.findById(itemId).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "영양제를 찾을 수 없습니다."));
            name = supplement.getSupplementName();
            names = supplement.getIngredients().stream().map(i -> i.getIngredientMaster().getIngredientName()).toList();
        }
        var candidate = CabinetItemResponseDto.builder().regId(-1L).type(type).itemId(itemId)
                .itemName(name).ingredientGuide(ingredientGuideService.assess(names)).build();
        var combined = new java.util.ArrayList<>(cabinet);
        combined.add(candidate);
        var result = CabinetIngredientOverlaps.enrich(combined).get(combined.size() - 1);
        boolean registered = cabinet.stream().anyMatch(i -> i.getType() == type && itemId.equals(i.getItemId()));
        long unverified = cabinet.stream().filter(i -> i.getIngredientGuide() == null
                || !"AVAILABLE".equals(i.getIngredientGuide().status())).count();
        return new MedicationPreviewDto(type, itemId, name, registered, result.getIngredientGuide(),
                result.getIngredientOverlaps(), unverified);
    }

    @Transactional
    public String deleteMedication(String loginId, Long regId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        UserMedicationReg medicationReg = userMedicationRegRepository.findById(regId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 등록 정보입니다."));
        if (!medicationReg.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("해당 약장을 삭제할 권한이 없습니다.");
        }
        userMedicationRegRepository.delete(medicationReg);
        return "내 약장에서 성공적으로 삭제되었습니다.";
    }

    private CabinetIngredientResponseDto toMedicineIngredientDto(MedicineIngredient ingredient) {
        return CabinetIngredientResponseDto.builder()
                .ingredientId(ingredient.getIngredientMaster().getId())
                .ingredientName(ingredient.getIngredientMaster().getIngredientName())
                .dosage(ingredient.getDosage())
                .unit(ingredient.getIngredientMaster().getUnit())
                .build();
    }

    private CabinetIngredientResponseDto toSupplementIngredientDto(SupplementIngredient ingredient) {
        return CabinetIngredientResponseDto.builder()
                .ingredientId(ingredient.getIngredientMaster().getId())
                .ingredientName(ingredient.getIngredientMaster().getIngredientName())
                .dosage(ingredient.getDosage())
                .unit(ingredient.getIngredientMaster().getUnit())
                .build();
    }
}
