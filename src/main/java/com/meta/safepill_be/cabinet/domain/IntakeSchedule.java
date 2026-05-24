package com.meta.safepill_be.cabinet.domain;

import com.meta.safepill_be.common.domain.TimeStamped;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "intake_schedule",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_intake_schedule_reg_time_day",
                        columnNames = {"reg_id", "take_time", "day_of_week"}
                )
        }
)
public class IntakeSchedule extends TimeStamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "take_time", nullable = false)
    private LocalTime takeTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private ScheduleDayOfWeek dayOfWeek;

    @Column(name = "dosage", nullable = false)
    private String dosage;

    @Column(name = "is_alarm_on", nullable = false)
    private boolean isAlarmOn = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reg_id", nullable = false)
    private UserMedicationReg userMedicationReg;

    @OneToMany(mappedBy = "intakeSchedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IntakeLog> logs = new ArrayList<>();
}
