package com.meta.safepill_be.cabinet.domain;

import com.meta.safepill_be.common.domain.TimeStamped;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "intake_log")
public class IntakeLog extends TimeStamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actual_time")
    private LocalDateTime actualTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private IntakeStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private IntakeSchedule intakeSchedule;
}