package com.meta.safepill_be.medicine.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
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

    @Column(columnDefinition = "TEXT", nullable = false)
    private String ingredients;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "appearance_info", columnDefinition = "json")
    private AppearanceInfo appearanceInfo;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String precautions;

    @OneToMany(mappedBy = "medicineMaster", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MedicineIngredient> ingredient = new ArrayList<>();
}
