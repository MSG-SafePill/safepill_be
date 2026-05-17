package com.meta.safepill_be.medicine.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class IngredientResponseDto {
    private Body body;
    private Response response;

    public Body getBody() {
        if (body != null) {
            return body;
        }
        return response != null ? response.getBody() : null;
    }

    @Getter
    @NoArgsConstructor
    public static class Response {
        private Body body;
    }

    @Getter
    @NoArgsConstructor
    public static class Body {
        private List<Item> items;
        private Integer totalCount;
    }

    @Getter
    @NoArgsConstructor
    public static class Item {
        @JsonProperty("ITEM_SEQ")
        private String itemSeq;

        @JsonProperty("MTRAL_NM")
        private String ingredientName;

        @JsonProperty("QNT")
        private String dosage;

        @JsonProperty("INGD_UNIT_CD")
        private String unit;
    }
}
