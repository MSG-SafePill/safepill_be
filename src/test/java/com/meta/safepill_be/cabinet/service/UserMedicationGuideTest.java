package com.meta.safepill_be.cabinet.service;

import com.meta.safepill_be.cabinet.domain.*;
import com.meta.safepill_be.cabinet.controller.UserMedicationController;
import com.meta.safepill_be.cabinet.repository.UserMedicationRegRepository;
import com.meta.safepill_be.medicine.domain.*;
import com.meta.safepill_be.medicine.guide.IngredientGuideService;
import com.meta.safepill_be.medicine.repository.*;
import com.meta.safepill_be.user.domain.User;
import com.meta.safepill_be.user.repository.UserRepository;
import com.meta.safepill_be.user.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserMedicationGuideTest {
    @Test
    void cabinetIncludesGuidesForBothItemTypesAndMissingProducts() throws Exception {
        var users = mock(UserRepository.class);
        var medicines = mock(MedicineMasterRepository.class);
        var supplements = mock(SupplementMasterRepository.class);
        var registrations = mock(UserMedicationRegRepository.class);
        var service = new UserMedicationService(users, medicines, supplements, registrations,
                new IngredientGuideService());
        var user = new User();
        user.setId(1L);
        when(users.findByLoginId("test")).thenReturn(Optional.of(user));
        when(registrations.findByUserId(1L)).thenReturn(List.of(
                registration(10L, ItemType.MEDICINE),
                registration(20L, ItemType.SUPPLEMENT),
                registration(30L, ItemType.MEDICINE)));
        when(medicines.findById(10L)).thenReturn(Optional.of(MedicineMaster.builder()
                .medicineName("테스트 약품")
                .ingredients(List.of(MedicineIngredient.builder()
                        .ingredientMaster(IngredientMaster.builder().id(100L)
                                .ingredientName("아세트아미노펜").build()).build()))
                .build()));
        when(supplements.findById(20L)).thenReturn(Optional.of(SupplementMaster.builder()
                .supplementName("테스트 영양제")
                .ingredients(List.of(SupplementIngredient.builder()
                        .ingredientMaster(IngredientMaster.builder().id(200L)
                                .ingredientName("비타민 C").build()).build()))
                .build()));
        when(medicines.findById(30L)).thenReturn(Optional.empty());

        var result = service.getMyMedications("test");
        assertEquals("ACETAMINOPHEN", result.get(0).getIngredientGuide().warnings().get(0).code());
        assertEquals("VITAMIN_C", result.get(1).getIngredientGuide().warnings().get(0).code());
        assertEquals("NO_INGREDIENT_DATA", result.get(2).getIngredientGuide().status());
        assertEquals(100L, result.get(0).getIngredients().get(0).getIngredientId());

        var jwt = mock(JwtUtil.class);
        when(jwt.getLoginIdFromToken("test-token")).thenReturn("test");
        var mvc = MockMvcBuilders.standaloneSetup(new UserMedicationController(service, jwt)).build();
        mvc.perform(get("/api/mypills").header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ingredientGuide.warnings[0].code").value("ACETAMINOPHEN"))
                .andExpect(jsonPath("$[1].ingredientGuide.status").value("AVAILABLE"))
                .andExpect(jsonPath("$[2].ingredientGuide.status").value("NO_INGREDIENT_DATA"));

        // A subsequent load reflects the repository after a removal.
        when(registrations.findByUserId(1L)).thenReturn(List.of());
        assertTrue(service.getMyMedications("test").isEmpty());
    }

    private UserMedicationReg registration(Long itemId, ItemType type) {
        var registration = new UserMedicationReg();
        registration.setId(itemId);
        registration.setItemId(itemId);
        registration.setItem_type(type);
        return registration;
    }
}
