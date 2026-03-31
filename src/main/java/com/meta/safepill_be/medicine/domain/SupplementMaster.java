package com.meta.safepill_be.medicine.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name= "supplement_master")
public class SupplementMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_seq", nullable = false, unique = true)
    private String itemSeq;

    @Column(name = "supplement_name", nullable = false)
    private String supplementName;

    @Column(name = "supplement_manufacturer", nullable = false)
    private String supplementManufacturer;

    @Column(columnDefinition = "TEXT")
    private String efficacy;

    @Column(columnDefinition = "TEXT")
    private String intakeMethod;

    @Column(columnDefinition = "TEXT")
    private String precautions;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "appearance_info", columnDefinition = "json")
    private AppearanceInfo appearanceInfo;

    @Builder.Default
    @OneToMany(mappedBy = "supplementMaster", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplementIngredient> ingredients = new ArrayList<>();
}