package com.meta.safepill_be.medicine.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DurResponseDto {
    private Body body;

    @Getter
    @Setter
    public static class Body {
        private List<ItemWrapper> items;
    }

    @Getter
    @Setter
    public static class ItemWrapper {
        private Item item;
    }

    @Getter
    @Setter
    public static class Item {
        @JsonProperty("INGR_KOR_NAME")
        private String ingredientA;

        @JsonProperty("MIXTURE_INGR_KOR_NAME")
        private String ingredientB;

        @JsonProperty("PROHBT_CONTENT")
        private String prohibitContent;
    }
}