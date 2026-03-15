package com.meta.safepill_be.cabinet.domain;

import com.meta.safepill_be.common.domain.TimeStamped;
import com.meta.safepill_be.user.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reg_id", nullable = false)
    private UserMedicationReg userMedicationReg;

    @OneToMany(mappedBy = "intakeSchedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IntakeLog> logs = new ArrayList<>();
}
