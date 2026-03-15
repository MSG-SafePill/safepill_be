package com.meta.safepill_be.cabinet.domain;

import com.meta.safepill_be.common.domain.TimeStamped;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "intake_schedule")
public class IntakeSchedule extends TimeStamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "time_slot", nullable = false)
    private String timeSlot;

    @Column(name = "is_alarm_on", nullable = false)
    private boolean isAlarmOn = true;
}
