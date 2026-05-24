package com.meta.safepill_be.medicine.dto;

import com.meta.safepill_be.medicine.domain.MedicineMaster;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MedicineSimpleDto {
    private Long id;
    private String itemSeq;
    private String medicineName;
    private String manufacturer;
    private String imageUrl;
    private boolean registered;

    public static MedicineSimpleDto from(MedicineMaster entity) {
        return from(entity, false);
    }

    public static MedicineSimpleDto from(MedicineMaster entity, boolean registered) {
        return MedicineSimpleDto.builder()
                .id(entity.getId())
                .itemSeq(entity.getItemSeq())
                .medicineName(entity.getMedicineName())
                .manufacturer(entity.getMedicineManufacturer())
                .imageUrl(entity.getAppearanceInfo() != null ? entity.getAppearanceInfo().getImageUrl() : null)
                .registered(registered)
                .build();
    }
}
