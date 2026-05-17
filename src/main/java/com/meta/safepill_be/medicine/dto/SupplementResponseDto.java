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
public class SupplementResponseDto {
    @JsonProperty("C003")
    private C003Data data;

    @Getter
    @NoArgsConstructor
    public static class C003Data {
        @JsonProperty("row")
        private List<Item> items;

        @JsonProperty("total_count")
        private Integer totalCount;
    }

    @Getter
    @NoArgsConstructor
    public static class Item {
        @JsonProperty("PRDLST_REPORT_NO")
        private String itemSeq;

        @JsonProperty("PRDLST_NM")
        private String supplementName;

        @JsonProperty("BSSH_NM")
        private String manufacturer;

        @JsonProperty("PRIMARY_FNCLTY")
        private String efficacy;

        @JsonProperty("NTK_MTHD")
        private String intakeMethod;

        @JsonProperty("IFTKN_ATNT_MATR_CN")
        private String precautions;

        @JsonProperty("DISPOS")
        private String dispos;

        @JsonProperty("RAWMTRL_NM")
        private String rawMaterial;

        @JsonAnySetter
        private Map<String, Object> unknownProperties = new HashMap<>();
    }
}
