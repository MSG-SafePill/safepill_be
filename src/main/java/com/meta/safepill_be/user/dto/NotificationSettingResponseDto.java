package com.meta.safepill_be.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationSettingResponseDto {
    private Long id;
    private Boolean allAlarmEnabled;
    private Boolean soundVibrateEnabled;
    private Boolean refillAlarmEnabled;
    private Integer snoozeMinutes;
    private String morningTime;
    private String lunchTime;
    private String dinnerTime;
    private String nightTime;
}
