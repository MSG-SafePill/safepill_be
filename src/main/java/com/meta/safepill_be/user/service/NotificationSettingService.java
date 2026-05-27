package com.meta.safepill_be.user.service;

import com.meta.safepill_be.user.domain.NotificationSetting;
import com.meta.safepill_be.user.domain.User;
import com.meta.safepill_be.user.dto.NotificationSettingRequestDto;
import com.meta.safepill_be.user.dto.NotificationSettingResponseDto;
import com.meta.safepill_be.user.repository.NotificationSettingRepository;
import com.meta.safepill_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
public class NotificationSettingService {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final UserRepository userRepository;
    private final NotificationSettingRepository notificationSettingRepository;

    @Transactional(readOnly = true)
    public NotificationSettingResponseDto getSettings(String loginId) {
        User user = getUser(loginId);
        NotificationSetting setting = notificationSettingRepository.findByUserId(user.getId())
                .orElseGet(() -> defaultSetting(user));
        return toResponseDto(setting);
    }

    @Transactional
    public NotificationSettingResponseDto upsertSettings(String loginId, NotificationSettingRequestDto requestDto) {
        User user = getUser(loginId);
        NotificationSetting setting = notificationSettingRepository.findByUserId(user.getId())
                .orElseGet(() -> defaultSetting(user));

        setting.setAllAlarmEnabled(valueOrDefault(requestDto.getAllAlarmEnabled(), setting.getAllAlarmEnabled()));
        setting.setSoundVibrateEnabled(valueOrDefault(requestDto.getSoundVibrateEnabled(), setting.getSoundVibrateEnabled()));
        setting.setRefillAlarmEnabled(valueOrDefault(requestDto.getRefillAlarmEnabled(), setting.getRefillAlarmEnabled()));
        setting.setSnoozeMinutes(validateSnoozeMinutes(
                valueOrDefault(requestDto.getSnoozeMinutes(), setting.getSnoozeMinutes())));
        setting.setMorningTime(parseTimeOrDefault(requestDto.getMorningTime(), setting.getMorningTime(), "아침 알림 시간"));
        setting.setLunchTime(parseTimeOrDefault(requestDto.getLunchTime(), setting.getLunchTime(), "점심 알림 시간"));
        setting.setDinnerTime(parseTimeOrDefault(requestDto.getDinnerTime(), setting.getDinnerTime(), "저녁 알림 시간"));
        setting.setNightTime(parseTimeOrDefault(requestDto.getNightTime(), setting.getNightTime(), "취침 알림 시간"));

        return toResponseDto(notificationSettingRepository.save(setting));
    }

    private User getUser(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private NotificationSetting defaultSetting(User user) {
        NotificationSetting setting = new NotificationSetting();
        setting.setUser(user);
        return setting;
    }

    private <T> T valueOrDefault(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    private Integer validateSnoozeMinutes(Integer value) {
        if (value == null || value < 1 || value > 180) {
            throw new IllegalArgumentException("스누즈 시간은 1분 이상 180분 이하로 입력해주세요.");
        }
        return value;
    }

    private LocalTime parseTimeOrDefault(String value, LocalTime defaultValue, String fieldName) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return LocalTime.parse(value.trim(), TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(fieldName + "은 HH:mm 형식이어야 합니다.");
        }
    }

    private NotificationSettingResponseDto toResponseDto(NotificationSetting setting) {
        return NotificationSettingResponseDto.builder()
                .id(setting.getId())
                .allAlarmEnabled(setting.getAllAlarmEnabled())
                .soundVibrateEnabled(setting.getSoundVibrateEnabled())
                .refillAlarmEnabled(setting.getRefillAlarmEnabled())
                .snoozeMinutes(setting.getSnoozeMinutes())
                .morningTime(formatTime(setting.getMorningTime()))
                .lunchTime(formatTime(setting.getLunchTime()))
                .dinnerTime(formatTime(setting.getDinnerTime()))
                .nightTime(formatTime(setting.getNightTime()))
                .build();
    }

    private String formatTime(LocalTime value) {
        return value.format(TIME_FORMATTER);
    }
}
