package com.meta.safepill_be.medicine.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DrugInfoResponseDto {
    private Body body;

    @Getter
    @Setter
    public static class Body {
        private List<Item> items;
    }

    @Getter
    @Setter
    public static class Item {
        private String itemSeq;
        private String itemName;
        private String efcyQesitm;
        private String useMethodQesitm;
        private String atpnWarnQesitm;
        private String atpnQesitm;
    }
}