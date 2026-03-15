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
@Table(name = "user_medication_reg")
public class UserMedicationReg extends TimeStamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private ItemType item_type;

    //참조ID, 촬영사진은 사용 방향성 확실하지 않아서 일단 보류
}
