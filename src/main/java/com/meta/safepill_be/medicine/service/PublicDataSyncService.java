package com.meta.safepill_be.medicine.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PublicDataSyncService {
    private final MedicineService medicineService;
    private final SupplementService supplementService;
    private final InteractionService interactionService;

    public void syncAll() {
        medicineService.fetchMedicineDataFromApi();
        supplementService.fetchAndSaveSupplements();
        medicineService.fetchAndSaveIngredients();
        medicineService.fetchAndUpdatePrecautions();
        medicineService.syncDrugInfoDetails(false);
        supplementService.fetchAndUpdateIngredientLimits();
        interactionService.fetchAndSaveInteractionRules();
    }
}
