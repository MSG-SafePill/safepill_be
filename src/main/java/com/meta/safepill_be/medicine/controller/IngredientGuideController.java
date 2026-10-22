package com.meta.safepill_be.medicine.controller;

import com.meta.safepill_be.medicine.guide.IngredientGuideCatalogEntry;
import com.meta.safepill_be.medicine.guide.IngredientGuideService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ingredient-guides")
public class IngredientGuideController {
    private final IngredientGuideService service;

    @GetMapping
    public List<IngredientGuideCatalogEntry> list() {
        return service.getCatalog();
    }
}
