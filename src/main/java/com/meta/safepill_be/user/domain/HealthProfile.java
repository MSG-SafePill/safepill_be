package com.meta.safepill_be.user.domain;

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
public class HealthProfile {
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}
