package com.meta.safepill_be.cabinet.dto;

import com.meta.safepill_be.cabinet.domain.ItemType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OcrRegisterResponseDto {
    private List<ItemResult> items;

    @Getter
    @Builder
    public static class ItemResult {
        private Long regId;
        private ItemType itemType;
        private Long itemId;
        private String itemName;
        private boolean alreadyRegistered;
        private List<IntakeScheduleResponseDto> schedules;
    }
}
