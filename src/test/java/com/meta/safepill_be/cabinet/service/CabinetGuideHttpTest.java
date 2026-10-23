package com.meta.safepill_be.cabinet.service;

import com.meta.safepill_be.medicine.domain.*;
import com.meta.safepill_be.medicine.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Real HTTP, JWT, services and persistence using only the isolated test H2 DB.
 * Synthetic products are never inserted into the development medication DB.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:guide_http;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1")
class CabinetGuideHttpTest {
    @LocalServerPort int port;
    @Autowired ObjectMapper json;
    @Autowired IngredientMasterRepository ingredients;
    @Autowired MedicineMasterRepository medicines;
    @Autowired SupplementMasterRepository supplements;
    @Autowired MedicineIngredientRepository medicineIngredients;
    @Autowired SupplementIngredientRepository supplementIngredients;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    @Test
    void catalogIsAvailableWithoutMedicationRegistrationOrLogin() throws Exception {
        var response = request("GET", "/api/ingredient-guides", null, null);
        assertEquals(200, response.statusCode());
        var entries = json.readTree(response.body());
        assertEquals(5, entries.size());
        Set<String> codes = new HashSet<>();
        boolean hasAlias = false;
        for (var entry : entries) {
            codes.add(entry.get("guide").get("code").asText());
            assertFalse(entry.get("guide").get("sourceUrl").asText().isBlank());
            assertFalse(entry.get("guide").get("detail").asText().isBlank());
            for (var alias : entry.get("aliases")) hasAlias |= alias.asText().equals("APAP");
        }
        assertEquals(5, codes.size());
        assertTrue(hasAlias);
    }

    @Test
    void signupLoginRegisterInspectAndDeleteThroughRealHttp() throws Exception {
        String loginId = "guide_" + UUID.randomUUID();
        var signup = Map.of("loginId", loginId, "password", "LocalTestOnly123!",
                "username", "가이드 테스트", "email", "guide@example.invalid",
                "gender", "MALE", "birthDate", "2000-01-01");
        assertEquals(200, request("POST", "/api/users/signup", signup, null).statusCode());
        var login = request("POST", "/api/users/login",
                Map.of("loginId", loginId, "password", "LocalTestOnly123!"), null);
        assertEquals(200, login.statusCode());
        String token = login.body();

        var medicine = medicines.save(MedicineMaster.builder()
                .itemSeq("GUIDE-TEST-MED").medicineName("합성 가이드 테스트 약품")
                .medicineManufacturer("테스트").precautions("테스트 전용").build());
        for (String name : List.of("아세트아미노펜", "모르핀", "카페인")) {
            medicineIngredients.save(MedicineIngredient.builder().medicineMaster(medicine)
                    .ingredientMaster(ingredient(name)).build());
        }
        var supplement = supplements.save(SupplementMaster.builder()
                .itemSeq("GUIDE-TEST-SUP").supplementName("합성 가이드 테스트 영양제")
                .supplementManufacturer("테스트").build());
        for (String name : List.of("칼슘", "비타민 C", "마그네슘", "APAP")) {
            supplementIngredients.save(SupplementIngredient.builder().supplementMaster(supplement)
                    .ingredientMaster(ingredient(name)).build());
        }
        var missing = medicines.save(MedicineMaster.builder()
                .itemSeq("GUIDE-TEST-MISSING").medicineName("성분 정보 없는 테스트 약품")
                .medicineManufacturer("테스트").precautions("테스트 전용").build());
        assertEquals(200, request("POST", "/api/mypills",
                Map.of("type", "MEDICINE", "itemId", medicine.getId()), token).statusCode());
        var preview = request("GET", "/api/mypills/preview?type=SUPPLEMENT&itemId=" + supplement.getId(), null, token);
        assertEquals(200, preview.statusCode());
        var previewBody = json.readTree(preview.body());
        assertFalse(previewBody.get("alreadyRegistered").asBoolean());
        assertEquals("ACETAMINOPHEN", previewBody.get("ingredientOverlaps").get(0).get("code").asText());
        assertEquals(1, previewBody.get("unverifiedCabinetItems").asInt());
        assertEquals(1, json.readTree(request("GET", "/api/mypills", null, token).body()).size());
        var registeredPreview = json.readTree(request("GET", "/api/mypills/preview?type=MEDICINE&itemId=" + medicine.getId(), null, token).body());
        assertTrue(registeredPreview.get("alreadyRegistered").asBoolean());
        assertTrue(registeredPreview.get("ingredientOverlaps").isEmpty());
        assertEquals(404, request("GET", "/api/mypills/preview?type=MEDICINE&itemId=999999999", null, token).statusCode());
        assertEquals(400, request("GET", "/api/mypills/preview?type=INVALID&itemId=1", null, token).statusCode());
        assertEquals(400, request("GET", "/api/mypills/preview?type=MEDICINE&itemId=-1", null, token).statusCode());
        var unknownPreview = json.readTree(request("GET", "/api/mypills/preview?type=MEDICINE&itemId=" + missing.getId(), null, token).body());
        assertEquals("NO_INGREDIENT_DATA", unknownPreview.get("ingredientGuide").get("status").asText());
        assertNotEquals(200, request("GET", "/api/mypills/preview?type=MEDICINE&itemId=" + medicine.getId(), null, null).statusCode());
        var otherSignup = new HashMap<String, String>(signup);
        otherSignup.put("loginId", loginId + "_other");
        assertEquals(200, request("POST", "/api/users/signup", otherSignup, null).statusCode());
        String otherToken = request("POST", "/api/users/login", Map.of("loginId", loginId + "_other", "password", "LocalTestOnly123!"), null).body();
        var otherPreview = json.readTree(request("GET", "/api/mypills/preview?type=SUPPLEMENT&itemId=" + supplement.getId(), null, otherToken).body());
        assertTrue(otherPreview.get("ingredientOverlaps").isEmpty());
        assertEquals(0, otherPreview.get("unverifiedCabinetItems").asInt());
        assertEquals(200, request("POST", "/api/mypills",
                Map.of("type", "SUPPLEMENT", "itemId", supplement.getId()), token).statusCode());
        assertEquals(200, request("POST", "/api/mypills",
                Map.of("type", "MEDICINE", "itemId", missing.getId()), token).statusCode());

        var response = request("GET", "/api/mypills", null, token);
        assertEquals(200, response.statusCode());
        var items = json.readTree(response.body());
        assertEquals(3, items.size());
        Set<String> codes = new HashSet<>();
        boolean partial = false;
        boolean missingData = false;
        int overlappingItems = 0;
        List<Long> registrationIds = new ArrayList<>();
        for (var item : items) {
            var overlaps = item.get("ingredientOverlaps");
            if (!overlaps.isEmpty()) {
                overlappingItems++;
                assertEquals("ACETAMINOPHEN", overlaps.get(0).get("code").asText());
                assertEquals(1, overlaps.get(0).get("relatedItems").size());
            }
            var assessment = item.get("ingredientGuide");
            partial |= assessment.get("status").asText().equals("PARTIAL");
            missingData |= assessment.get("status").asText().equals("NO_INGREDIENT_DATA");
            for (var warning : assessment.get("warnings")) {
                codes.add(warning.get("code").asText());
                assertTrue(warning.get("sourceUrl").asText().startsWith("https://"));
            }
            registrationIds.add(item.get("regId").asLong());
        }
        assertEquals(Set.of("CALCIUM", "VITAMIN_C", "MAGNESIUM", "ACETAMINOPHEN", "MORPHINE"), codes);
        assertTrue(partial);
        assertTrue(missingData);
        assertEquals(2, overlappingItems);
        for (Long regId : registrationIds) {
            assertEquals(200, request("DELETE", "/api/mypills/" + regId, null, token).statusCode());
        }
        assertEquals(0, json.readTree(request("GET", "/api/mypills", null, token).body()).size());
    }

    private IngredientMaster ingredient(String name) {
        return ingredients.save(IngredientMaster.builder()
                .ingredientName(name).bestTimeGuide("테스트 전용").build());
    }

    private HttpResponse<String> request(String method, String path, Object body, String token) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(20)).header("Content-Type", "application/json");
        if (token != null) builder.header("Authorization", "Bearer " + token);
        builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}
