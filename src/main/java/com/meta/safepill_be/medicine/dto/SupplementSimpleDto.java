package com.meta.safepill_be.medicine.dto;

import com.meta.safepill_be.medicine.domain.SupplementMaster;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SupplementSimpleDto {
    private Long id;
    private String itemSeq;
    private String supplementName;
    private String manufacturer;
    private boolean registered;

    public static SupplementSimpleDto from(SupplementMaster entity) {
        return from(entity, false);
    }

    public static SupplementSimpleDto from(SupplementMaster entity, boolean registered) {
        return SupplementSimpleDto.builder()
                .id(entity.getId())
                .itemSeq(entity.getItemSeq())
                .supplementName(entity.getSupplementName())
                .manufacturer(entity.getSupplementManufacturer())
                .registered(registered)
                .build();
    }
}
