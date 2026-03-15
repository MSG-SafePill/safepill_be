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
@Table(name= "supplement_master")
public class SupplementMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prdlst_report_no", nullable = false, unique = true)
    private String prdlstReportNo;

    @Column(name = "supplement_name", nullable = false)
    private String supplementName;

    @Column(name = "medicine_manufacturer", nullable = false)
    private String medicineManufacturer;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "appearance_info", columnDefinition = "json")
    private AppearanceInfo appearanceInfo;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String precautions;

    @OneToMany(mappedBy = "supplementMaster", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplementIngredient> ingredients = new ArrayList<>();
}
