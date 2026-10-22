package com.meta.safepill_be.cabinet.service;

import com.meta.safepill_be.cabinet.dto.CabinetItemResponseDto;
import com.meta.safepill_be.cabinet.dto.CabinetIngredientOverlap;
import java.util.*;

/** Compares only the five curated guide codes within one user's cabinet. */
final class CabinetIngredientOverlaps {
    private CabinetIngredientOverlaps() {}

    static List<CabinetItemResponseDto> enrich(List<CabinetItemResponseDto> items) {
        Map<String, Map<Long, CabinetItemResponseDto>> byCode = new LinkedHashMap<>();
        for (var item : items) {
            if (item.getIngredientGuide() == null) continue;
            for (var guide : item.getIngredientGuide().warnings()) {
                byCode.computeIfAbsent(guide.code(), key -> new LinkedHashMap<>())
                        .putIfAbsent(item.getRegId(), item);
            }
        }
        return items.stream().map(item -> {
            List<CabinetIngredientOverlap> overlaps = new ArrayList<>();
            Set<String> seen = new HashSet<>();
            if (item.getIngredientGuide() != null) {
                for (var guide : item.getIngredientGuide().warnings()) {
                    if (!seen.add(guide.code())) continue;
                    var related = byCode.get(guide.code()).values().stream()
                            .filter(other -> !Objects.equals(other.getRegId(), item.getRegId()))
                            .filter(other -> !(other.getType() == item.getType()
                                    && Objects.equals(other.getItemId(), item.getItemId())))
                            .map(other -> new CabinetIngredientOverlap.RelatedItem(other.getRegId(), other.getItemName()))
                            .toList();
                    if (!related.isEmpty()) {
                        overlaps.add(new CabinetIngredientOverlap(guide.code(), guide.ingredientName(), related));
                    }
                }
            }
            return item.toBuilder().ingredientOverlaps(List.copyOf(overlaps)).build();
        }).toList();
    }
}
