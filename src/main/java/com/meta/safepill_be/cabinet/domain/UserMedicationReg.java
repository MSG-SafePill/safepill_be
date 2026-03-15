package com.meta.safepill_be.cabinet.domain;

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
@Table(name = "user_medication_reg")
public class UserMedicationReg {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private ItemType item_type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // DB에 생성될 외래키 컬럼명
    private User user;

    @OneToMany(mappedBy = "userMedicationReg", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IntakeSchedule> schedules = new ArrayList<>();

    //참조ID, 촬영사진은 사용 방향성 확실하지 않아서 일단 보류
}
