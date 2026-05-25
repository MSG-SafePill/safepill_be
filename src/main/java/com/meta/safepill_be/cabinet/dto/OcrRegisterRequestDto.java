package com.meta.safepill_be.cabinet.dto;

import com.meta.safepill_be.cabinet.domain.ItemType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class OcrRegisterRequestDto {
    private List<Item> items;

    @Getter
    @NoArgsConstructor
    public static class Item {
        private ItemType itemType;
        private Long itemId;
        private List<Schedule> schedules;
    }

    @Getter
    @NoArgsConstructor
    public static class Schedule {
        private String takeTime;
        private List<String> daysOfWeek;
        private String dosage;
    }
}
