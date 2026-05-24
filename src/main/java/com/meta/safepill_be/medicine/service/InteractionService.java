package com.meta.safepill_be.medicine.service;

import com.meta.safepill_be.cabinet.domain.ItemType;
import com.meta.safepill_be.cabinet.domain.UserMedicationReg;
import com.meta.safepill_be.cabinet.repository.UserMedicationRegRepository;
import com.meta.safepill_be.medicine.domain.IngredientMaster;
import com.meta.safepill_be.medicine.domain.InteractionRule;
import com.meta.safepill_be.medicine.domain.MedicineIngredient;
import com.meta.safepill_be.medicine.domain.MedicineMaster;
import com.meta.safepill_be.medicine.domain.RiskLevel;
import com.meta.safepill_be.medicine.domain.SupplementIngredient;
import com.meta.safepill_be.medicine.domain.SupplementMaster;
import com.meta.safepill_be.medicine.dto.DurResponseDto;
import com.meta.safepill_be.medicine.dto.InteractionAnalyzeResponseDto;
import com.meta.safepill_be.medicine.repository.IngredientMasterRepository;
import com.meta.safepill_be.medicine.repository.InteractionRuleRepository;
import com.meta.safepill_be.medicine.repository.MedicineMasterRepository;
import com.meta.safepill_be.medicine.repository.SupplementMasterRepository;
import com.meta.safepill_be.user.domain.User;
import com.meta.safepill_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InteractionService {
    private final InteractionRuleRepository interactionRuleRepository;
    private final IngredientMasterRepository ingredientMasterRepository;
    private final MedicineMasterRepository medicineMasterRepository;
    private final SupplementMasterRepository supplementMasterRepository;
    private final UserMedicationRegRepository userMedicationRegRepository;
    private final UserRepository userRepository;
    @Value("${open-api.data-go-kr.base-url}")
    private String baseUrl;

    @Value("${open-api.data-go-kr.service-key}")
    private String serviceKey;

    @Value("${open-api.data-go-kr.endpoint.dur-taboo}")
    private String durTabooEndpoint;

    @Transactional
    public void fetchAndSaveInteractionRules() {
        WebClient webClient = createWebClient();
        System.out.println("🚀 병용금기(상극) 데이터 파싱 및 DB 저장을 시작합니다...");
        int totalSaved = 0;
        int page = 1;
        int numOfRows = 500;
        try {
            while (true) {
                final int currentPage = page;
                System.out.println("📄 " + page + "페이지 데이터 요청 중...");
                DurResponseDto response = webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path(durTabooEndpoint)
                                .queryParam("serviceKey", serviceKey)
                                .queryParam("type", "json")
                                .queryParam("numOfRows", String.valueOf(numOfRows))
                                .queryParam("pageNo", String.valueOf(currentPage))
                                .build())
                        .retrieve()
                        .bodyToMono(DurResponseDto.class)
                        .block();
                if (response == null || response.getBody() == null || response.getBody().getItems() == null) {
                    System.out.println("⚠️ " + page + "페이지에 데이터가 없거나 서버 응답이 이상합니다. 수집을 종료합니다.");
                    break;
                }
                List<DurResponseDto.ItemWrapper> wrappers = response.getBody().getItems();
                int pageCount = 0;
                for (DurResponseDto.ItemWrapper wrapper : wrappers) {
                    DurResponseDto.Item item = wrapper.getItem();
                    if (item == null) continue;
                    String nameA = item.getIngredientA();
                    String nameB = item.getIngredientB();
                    String desc = item.getProhibitContent();
                    if (nameA == null || nameB == null || nameA.isEmpty() || nameB.isEmpty()) continue;
                    IngredientMaster ingredientA = findOrCreateIngredient(nameA);
                    IngredientMaster ingredientB = findOrCreateIngredient(nameB);
                    if (interactionRuleRepository.existsByIngredientA_IdAndIngredientB_Id(
                            ingredientA.getId(), ingredientB.getId())
                            || interactionRuleRepository.existsByIngredientA_IdAndIngredientB_Id(
                            ingredientB.getId(), ingredientA.getId())) {
                        continue;
                    }
                    InteractionRule rule = new InteractionRule();
                    rule.setIngredientA(ingredientA);
                    rule.setIngredientB(ingredientB);
                    rule.setRiskLevel(RiskLevel.DANGER);
                    rule.setDescription(desc != null ? desc : "병용금기 성분입니다.");
                    interactionRuleRepository.save(rule);
                    pageCount++;
                }
                totalSaved += pageCount;
                System.out.println("✅ " + page + "페이지 저장 완료! (누적: " + totalSaved + "건)");
                Integer totalCount = response.getBody().getTotalCount();
                if (totalCount != null && currentPage * numOfRows >= totalCount) {
                    break;
                }
                if (wrappers.size() < numOfRows) {
                    break;
                }
                page++;
                Thread.sleep(100);
            }
            System.out.println("🎉 대규모 상극 데이터 수집 대성공! 총 " + totalSaved + "건 동기화 완료!");
        } catch (Exception e) {
            System.err.println("❌ 상극 데이터 수집 중 에러: " + e.getMessage());
        }
    }

    private IngredientMaster findOrCreateIngredient(String name) {
        return ingredientMasterRepository.findByIngredientName(name)
                .orElseGet(() -> ingredientMasterRepository.save(
                        IngredientMaster.builder()
                                .ingredientName(name)
                                .bestTimeGuide("정보 없음")
                                .intakeTip("정보 없음")
                                .build()
                ));
    }

    private WebClient createWebClient() {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(baseUrl);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);
        return WebClient.builder().uriBuilderFactory(factory).build();
    }

    @Transactional(readOnly = true)
    public List<InteractionAnalyzeResponseDto> analyzeInteractions(List<Long> medicineIds) {
        List<InteractionAnalyzeResponseDto> result = new java.util.ArrayList<>();
        List<com.meta.safepill_be.medicine.domain.MedicineMaster> medicines = medicineMasterRepository.findAllById(medicineIds);
        java.util.Map<Long, java.util.Set<com.meta.safepill_be.medicine.domain.MedicineMaster>> ingredientToMedicinesMap = new java.util.HashMap<>();
        java.util.Set<Long> allIngredientIds = new java.util.HashSet<>();
        for (com.meta.safepill_be.medicine.domain.MedicineMaster medicine : medicines) {
            for (com.meta.safepill_be.medicine.domain.MedicineIngredient mi : medicine.getIngredients()) {
                Long ingId = mi.getIngredientMaster().getId();
                allIngredientIds.add(ingId);
                ingredientToMedicinesMap.computeIfAbsent(ingId, k -> new java.util.HashSet<>()).add(medicine);
            }
        }
        if (allIngredientIds.isEmpty()) return result;
        List<InteractionRule> triggeredRules = interactionRuleRepository.findInteractionsByIngredientIds(new java.util.ArrayList<>(allIngredientIds));
        java.util.Set<String> alreadyAddedPairs = new java.util.HashSet<>();
        for (InteractionRule rule : triggeredRules) {
            Long ingAId = rule.getIngredientA().getId();
            Long ingBId = rule.getIngredientB().getId();
            java.util.Set<com.meta.safepill_be.medicine.domain.MedicineMaster> medsContainingA = ingredientToMedicinesMap.get(ingAId);
            java.util.Set<com.meta.safepill_be.medicine.domain.MedicineMaster> medsContainingB = ingredientToMedicinesMap.get(ingBId);
            if (medsContainingA == null || medsContainingB == null) continue;
            for (com.meta.safepill_be.medicine.domain.MedicineMaster medA : medsContainingA) {
                for (com.meta.safepill_be.medicine.domain.MedicineMaster medB : medsContainingB) {
                    if (!medA.getId().equals(medB.getId())) {
                        Long minId = Math.min(medA.getId(), medB.getId());
                        Long maxId = Math.max(medA.getId(), medB.getId());
                        String pairKey = minId + "_" + maxId;
                        if (!alreadyAddedPairs.contains(pairKey)) {
                            alreadyAddedPairs.add(pairKey); // 명부에 등록
                            result.add(InteractionAnalyzeResponseDto.builder()
                                    .medicineNameA(medA.getMedicineName())
                                    .medicineNameB(medB.getMedicineName())
                                    .ingredientNameA(rule.getIngredientA().getIngredientName())
                                    .ingredientNameB(rule.getIngredientB().getIngredientName())
                                    .riskLevel(rule.getRiskLevel())
                                    .description(rule.getDescription())
                                    .build());
                        }
                    }
                }
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<InteractionAnalyzeResponseDto> analyzeMyCabinetInteractions(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        List<UserMedicationReg> registrations = userMedicationRegRepository.findByUserId(user.getId());
        if (registrations.size() < 2) {
            return List.of();
        }

        List<CabinetIngredientSource> sources = buildCabinetIngredientSources(registrations);
        Map<Long, Set<CabinetIngredientSource>> ingredientToSources = new HashMap<>();
        for (CabinetIngredientSource source : sources) {
            ingredientToSources.computeIfAbsent(source.ingredientId(), key -> new HashSet<>()).add(source);
        }

        if (ingredientToSources.size() < 2) {
            return List.of();
        }

        List<InteractionRule> triggeredRules = interactionRuleRepository.findInteractionsByIngredientIds(
                new ArrayList<>(ingredientToSources.keySet()));
        List<InteractionAnalyzeResponseDto> result = new ArrayList<>();
        Set<String> alreadyAdded = new HashSet<>();

        for (InteractionRule rule : triggeredRules) {
            Set<CabinetIngredientSource> sourcesA = ingredientToSources.get(rule.getIngredientA().getId());
            Set<CabinetIngredientSource> sourcesB = ingredientToSources.get(rule.getIngredientB().getId());
            if (sourcesA == null || sourcesB == null) {
                continue;
            }

            for (CabinetIngredientSource sourceA : sourcesA) {
                for (CabinetIngredientSource sourceB : sourcesB) {
                    if (sourceA.regId().equals(sourceB.regId())) {
                        continue;
                    }

                    Long minRegId = Math.min(sourceA.regId(), sourceB.regId());
                    Long maxRegId = Math.max(sourceA.regId(), sourceB.regId());
                    Long minIngredientId = Math.min(rule.getIngredientA().getId(), rule.getIngredientB().getId());
                    Long maxIngredientId = Math.max(rule.getIngredientA().getId(), rule.getIngredientB().getId());
                    String key = minRegId + "_" + maxRegId + "_" + minIngredientId + "_" + maxIngredientId;
                    if (!alreadyAdded.add(key)) {
                        continue;
                    }

                    result.add(InteractionAnalyzeResponseDto.builder()
                            .itemNameA(sourceA.itemName())
                            .itemNameB(sourceB.itemName())
                            .itemTypeA(sourceA.itemType())
                            .itemTypeB(sourceB.itemType())
                            .medicineNameA(sourceA.itemName())
                            .medicineNameB(sourceB.itemName())
                            .ingredientNameA(rule.getIngredientA().getIngredientName())
                            .ingredientNameB(rule.getIngredientB().getIngredientName())
                            .riskLevel(rule.getRiskLevel())
                            .description(rule.getDescription())
                            .build());
                }
            }
        }

        return result;
    }

    private List<CabinetIngredientSource> buildCabinetIngredientSources(List<UserMedicationReg> registrations) {
        Map<Long, UserMedicationReg> medicineRegsByItemId = registrations.stream()
                .filter(reg -> reg.getItem_type() == ItemType.MEDICINE)
                .collect(Collectors.toMap(UserMedicationReg::getItemId, Function.identity(), (left, right) -> left));
        Map<Long, UserMedicationReg> supplementRegsByItemId = registrations.stream()
                .filter(reg -> reg.getItem_type() == ItemType.SUPPLEMENT)
                .collect(Collectors.toMap(UserMedicationReg::getItemId, Function.identity(), (left, right) -> left));

        List<CabinetIngredientSource> sources = new ArrayList<>();
        List<MedicineMaster> medicines = medicineRegsByItemId.isEmpty()
                ? List.of()
                : medicineMasterRepository.findByIdIn(new ArrayList<>(medicineRegsByItemId.keySet()));
        for (MedicineMaster medicine : medicines) {
            UserMedicationReg reg = medicineRegsByItemId.get(medicine.getId());
            if (reg == null) {
                continue;
            }
            for (MedicineIngredient ingredient : medicine.getIngredients()) {
                sources.add(new CabinetIngredientSource(
                        reg.getId(),
                        ItemType.MEDICINE,
                        medicine.getMedicineName(),
                        ingredient.getIngredientMaster().getId()));
            }
        }

        List<SupplementMaster> supplements = supplementRegsByItemId.isEmpty()
                ? List.of()
                : supplementMasterRepository.findByIdIn(new ArrayList<>(supplementRegsByItemId.keySet()));
        for (SupplementMaster supplement : supplements) {
            UserMedicationReg reg = supplementRegsByItemId.get(supplement.getId());
            if (reg == null) {
                continue;
            }
            for (SupplementIngredient ingredient : supplement.getIngredients()) {
                sources.add(new CabinetIngredientSource(
                        reg.getId(),
                        ItemType.SUPPLEMENT,
                        supplement.getSupplementName(),
                        ingredient.getIngredientMaster().getId()));
            }
        }

        return sources;
    }

    private record CabinetIngredientSource(
            Long regId,
            ItemType itemType,
            String itemName,
            Long ingredientId
    ) {
    }
}
