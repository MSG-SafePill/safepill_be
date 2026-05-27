package com.meta.safepill_be.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NotificationSettingRequestDto {
    private Boolean allAlarmEnabled;
    private Boolean soundVibrateEnabled;
    private Boolean refillAlarmEnabled;
    private Integer snoozeMinutes;
    private String morningTime;
    private String lunchTime;
    private String dinnerTime;
    private String nightTime;
}
