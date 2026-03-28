package com.meta.safepill_be.medicine.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "medicine_master")
public class MedicineMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_seq", nullable = false, unique = true)
    private String itemSeq; // 품목기준코드

    @Column(name = "medicine_name", nullable = false)
    private String medicineName;

    @Column(name = "medicine_manufacturer", nullable = false)
    private String medicineManufacturer;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "appearance_info", columnDefinition = "json")
    private AppearanceInfo appearanceInfo;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String precautions;

    @Builder.Default
    @OneToMany(mappedBy = "medicineMaster", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MedicineIngredient> ingredients = new ArrayList<>();
}
