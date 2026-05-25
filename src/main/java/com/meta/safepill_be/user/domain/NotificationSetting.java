package com.meta.safepill_be.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "notification_setting")
public class NotificationSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "all_alarm_enabled", nullable = false)
    private Boolean allAlarmEnabled = true;

    @Column(name = "sound_vibrate_enabled", nullable = false)
    private Boolean soundVibrateEnabled = false;

    @Column(name = "refill_alarm_enabled", nullable = false)
    private Boolean refillAlarmEnabled = true;

    @Column(name = "snooze_minutes", nullable = false)
    private Integer snoozeMinutes = 10;

    @Column(name = "morning_time", nullable = false)
    private LocalTime morningTime = LocalTime.of(8, 30);

    @Column(name = "lunch_time", nullable = false)
    private LocalTime lunchTime = LocalTime.of(13, 0);

    @Column(name = "dinner_time", nullable = false)
    private LocalTime dinnerTime = LocalTime.of(19, 0);

    @Column(name = "night_time", nullable = false)
    private LocalTime nightTime = LocalTime.of(23, 30);

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}
