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
public class IngredientLimitResponseDto {
    private Map<String, ServiceData> dynamicData = new HashMap<>();

    @JsonAnySetter
    public void setDynamicData(String key, ServiceData value) {
        this.dynamicData.put(key, value);
    }

    // 껍데기 벗기고 진짜 데이터(row)만 쏙 빼주는 편의 메서드
    public List<Item> getItems() {
        if (dynamicData.isEmpty()) return null;
        return dynamicData.values().iterator().next().getItems();
    }

    @Getter
    @NoArgsConstructor
    public static class ServiceData {
        @JsonProperty("row")
        private List<Item> items;
    }

    @Getter
    @NoArgsConstructor
    public static class Item {
        @JsonProperty("SKLL_IX_IRDNT_RAWMTRL")
        private String ingredientName; // 성분명

        @JsonProperty("DAY_INTK_HIGHLIMIT")
        private String upperLimit; // 일일섭취량 상한

        @JsonProperty("INTK_UNIT")
        private String unit; // 단위
    }
}