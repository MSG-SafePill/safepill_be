package com.meta.safepill_be.user.service;

import com.meta.safepill_be.user.domain.HealthProfile;
import com.meta.safepill_be.user.domain.User;
import com.meta.safepill_be.user.dto.HealthProfileRequestDto;
import com.meta.safepill_be.user.dto.HealthProfileResponseDto;
import com.meta.safepill_be.user.repository.HealthProfileRepository;
import com.meta.safepill_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HealthProfileService {
    private final UserRepository userRepository;
    private final HealthProfileRepository healthProfileRepository;

    @Transactional(readOnly = true)
    public HealthProfileResponseDto getHealthProfile(String loginId) {
        User user = getUser(loginId);
        return healthProfileRepository.findByUserId(user.getId())
                .map(this::toResponseDto)
                .orElse(null);
    }

    @Transactional
    public HealthProfileResponseDto upsertHealthProfile(String loginId, HealthProfileRequestDto requestDto) {
        User user = getUser(loginId);
        HealthProfile healthProfile = healthProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    HealthProfile created = new HealthProfile();
                    created.setUser(user);
                    return created;
                });

        healthProfile.setDisease(normalizeRequiredText(requestDto.getDisease(), "질환 정보"));
        healthProfile.setAllergy(normalizeRequiredText(requestDto.getAllergy(), "알레르기 정보"));
        healthProfile.setCustomGuide(requestDto.getCustomGuide());
        return toResponseDto(healthProfileRepository.save(healthProfile));
    }

    @Transactional
    public String deleteHealthProfile(String loginId) {
        User user = getUser(loginId);
        HealthProfile healthProfile = healthProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("등록된 건강 프로필이 없습니다."));
        healthProfileRepository.delete(healthProfile);
        return "건강 프로필이 삭제되었습니다.";
    }

    private User getUser(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private String normalizeRequiredText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return "없음";
        }
        return value.trim();
    }

    private HealthProfileResponseDto toResponseDto(HealthProfile healthProfile) {
        return HealthProfileResponseDto.builder()
                .id(healthProfile.getId())
                .disease(healthProfile.getDisease())
                .allergy(healthProfile.getAllergy())
                .customGuide(healthProfile.getCustomGuide())
                .build();
    }
}
