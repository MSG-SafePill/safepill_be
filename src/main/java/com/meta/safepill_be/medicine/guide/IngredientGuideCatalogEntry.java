package com.meta.safepill_be.medicine.guide;

import java.util.List;

public record IngredientGuideCatalogEntry(IngredientGuide guide, List<String> aliases) {}
