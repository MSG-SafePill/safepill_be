package com.meta.safepill_be.medicine.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class AiInteractionWarningDto {
    private String title;
    private String severity;
    private List<String> items;
    private String reason;
}
