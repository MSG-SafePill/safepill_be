package com.meta.safepill_be.medicine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiInteractionWarningDto {
    private String title;
    private String severity;
    private List<String> items;
    private String reason;
}
