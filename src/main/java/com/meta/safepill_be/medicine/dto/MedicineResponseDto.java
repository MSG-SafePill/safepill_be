package com.meta.safepill_be.medicine.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class MedicineResponseDto {
    private Body body;

    @Getter
    @NoArgsConstructor
    public static class Body {
        private List<Item> items;
    }

    @Getter
    @NoArgsConstructor
    public static class Item {
        @JsonProperty("ITEM_SEQ")
        private String itemSeq;

        @JsonProperty("ITEM_NAME")
        private String medicineName;

        @JsonProperty("ENTP_NAME")
        private String medicineManufacturer;

        @JsonProperty("DRUG_SHAPE")
        private String shape;

        @JsonProperty("COLOR_CLASS1")
        private String color;

        @JsonProperty("CHART")
        private String formulation;

        @JsonProperty("ITEM_IMAGE")
        private String imageUrl;

        @JsonProperty("LINE_FRONT")
        private String lineFront;

        @JsonProperty("LINE_BACK")
        private String lineBack;

        @JsonProperty("PRINT_FRONT")
        private String printFront;

        @JsonProperty("PRINT_BACK")
        private String printBack;
    }
}