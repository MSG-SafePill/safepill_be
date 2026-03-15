package com.meta.safepill_be.user.domain;

import com.meta.safepill_be.common.domain.TimeStamped;
import com.meta.safepill_be.medicine.domain.CustomGuideInfo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "health_profile")
public class HealthProfile extends TimeStamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String disease;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String allergy;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_guide", columnDefinition = "json")
    private CustomGuideInfo customGuide;
}
