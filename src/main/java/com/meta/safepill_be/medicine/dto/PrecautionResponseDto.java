package com.meta.safepill_be.medicine.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
public class PrecautionResponseDto {
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

        @JsonProperty("EE_DOC_DATA")
        private String efficacy;

        @JsonProperty("UD_DOC_DATA")
        private String useMethod;

        @JsonProperty("NB_DOC_DATA")
        private String precautions;

        @JsonProperty("MATERIAL_NAME")
        private String materials;

        @JsonProperty("MAIN_ITEM_INGR")
        private String mainIngredients;

        @JsonProperty("CANCEL_NAME")
        private String cancelName;

        @JsonProperty("CANCEL_DATE")
        private String cancelDate;

        @JsonProperty("ATC_CODE")
        private String atcCode;

        @JsonAnySetter
        private Map<String, Object> unknownProperties = new HashMap<>();
    }
}
