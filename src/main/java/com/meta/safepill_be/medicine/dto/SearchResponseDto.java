package com.meta.safepill_be.medicine.dto;

import com.meta.safepill_be.medicine.domain.MedicineMaster;
import com.meta.safepill_be.medicine.domain.SupplementMaster;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SearchResponseDto {
    private List<MedicineMaster> medicines;
    private List<SupplementMaster> supplements;
}
