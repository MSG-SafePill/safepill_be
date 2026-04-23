package com.meta.safepill_be.cabinet.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.meta.safepill_be.cabinet.domain.ItemType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonPropertyOrder({"regId", "type", "itemId", "itemName"})
public class CabinetItemResponseDto {
    private Long regId;
    private ItemType type;
    private Long itemId;
    private String itemName;
}