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

    @Column(columnDefinition = "TEXT")
    private String efficacy;

    @Column(columnDefinition = "TEXT")
    private String useMethod;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String precautions;

    @Column(name = "cancel_name")
    private String cancelName; // 허가 상태(정상/취하/유효기간만료 등). null이면 아직 확인 전

    @Column(name = "cancel_date")
    private String cancelDate;

    @Column(name = "atc_code")
    private String atcCode; // WHO ATC 약효 분류 코드

    @Builder.Default
    @OneToMany(mappedBy = "medicineMaster", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MedicineIngredient> ingredients = new ArrayList<>();

    public void updateDetails(String efficacy, String useMethod, String precautions) {
        this.efficacy = efficacy;
        this.useMethod = useMethod;
        this.precautions = precautions;
    }

    public void updatePermitInfo(String cancelName, String cancelDate, String atcCode) {
        this.cancelName = cancelName;
        this.cancelDate = cancelDate;
        this.atcCode = atcCode;
    }

    /** 허가가 유효한 제품인지. 상태를 아직 모르는 경우(null)는 유효로 본다. */
    public boolean isPermitActive() {
        return cancelName == null || cancelName.isBlank() || "정상".equals(cancelName.trim());
    }
}
