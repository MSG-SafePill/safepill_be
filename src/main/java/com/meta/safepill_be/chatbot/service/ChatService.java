package com.meta.safepill_be.chatbot.service;

import com.meta.safepill_be.cabinet.domain.ItemType;
import com.meta.safepill_be.cabinet.domain.UserMedicationReg;
import com.meta.safepill_be.cabinet.repository.IntakeScheduleRepository;
import com.meta.safepill_be.cabinet.repository.UserMedicationRegRepository;
import com.meta.safepill_be.chatbot.domain.ChatMessage;
import com.meta.safepill_be.chatbot.domain.ChatSession;
import com.meta.safepill_be.chatbot.domain.SenderRole;
import com.meta.safepill_be.chatbot.dto.*;
import com.meta.safepill_be.chatbot.repository.ChatMessageRepository;
import com.meta.safepill_be.chatbot.repository.ChatSessionRepository;
import com.meta.safepill_be.medicine.domain.MedicineMaster;
import com.meta.safepill_be.medicine.domain.SupplementMaster;
import com.meta.safepill_be.medicine.repository.MedicineMasterRepository;
import com.meta.safepill_be.medicine.repository.SupplementMasterRepository;
import com.meta.safepill_be.user.domain.User;
import com.meta.safepill_be.user.repository.HealthProfileRepository;
import com.meta.safepill_be.user.repository.UserRepository;
import com.meta.safepill_be.vision.client.PythonAiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final UserRepository userRepository;
    private final UserMedicationRegRepository userMedicationRegRepository;
    private final MedicineMasterRepository medicineRepository;
    private final SupplementMasterRepository supplementRepository;
    private final IntakeScheduleRepository intakeScheduleRepository;
    private final HealthProfileRepository healthProfileRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final PythonAiClient pythonAiClient;

    @Transactional
    public ChatSessionResponseDto createSession(String loginId) {
        User user = getUser(loginId);
        ChatSession session = new ChatSession();
        session.setUser(user);
        session.setStartedAt(LocalDateTime.now());
        return toSessionResponse(chatSessionRepository.save(session));
    }

    @Transactional(readOnly = true)
    public List<ChatSessionResponseDto> getSessions(String loginId) {
        User user = getUser(loginId);
        return chatSessionRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::toSessionResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponseDto> getMessages(String loginId, Long sessionId) {
        User user = getUser(loginId);
        ChatSession session = getOwnedSession(sessionId, user);
        return chatMessageRepository.findByChatSession_IdOrderByCreatedAtAsc(session.getId()).stream()
                .map(this::toMessageResponse)
                .toList();
    }

    @Transactional
    public ChatAnswerResponseDto ask(String loginId, Long sessionId, ChatQuestionRequestDto requestDto) {
        if (requestDto.getQuestion() == null || requestDto.getQuestion().isBlank()) {
            throw new IllegalArgumentException("질문은 필수입니다.");
        }

        User user = getUser(loginId);
        ChatSession session = getOwnedSession(sessionId, user);
        ChatMessage userMessage = saveMessage(session, SenderRole.User, requestDto.getQuestion().trim());

        CabinetContext cabinetContext = resolveCabinetContext(user, requestDto);
        AiAnswer aiAnswer = isCabinetListQuestion(requestDto.getQuestion())
                ? answerCabinetList(cabinetContext.referencedPills())
                : answerWithAi(
                        requestDto.getQuestion().trim(),
                        cabinetContext.referencedPills(),
                        cabinetContext.contextItems(),
                        cabinetContext.userProfile());
        ChatMessage assistantMessage = saveMessage(session, SenderRole.System, aiAnswer.answer());

        return ChatAnswerResponseDto.builder()
                .userMessage(toMessageResponse(userMessage))
                .assistantMessage(toMessageResponse(assistantMessage))
                .referencedPills(aiAnswer.referencedPills())
                .fallback(aiAnswer.fallback())
                .build();
    }

    private AiAnswer answerWithAi(
            String question,
            List<String> referencedPills,
            List<Map<String, Object>> contextItems,
            Map<String, Object> userProfile
    ) {
        if (referencedPills.isEmpty()) {
            return new AiAnswer(
                    "내 약장에 등록된 약품이 없거나 상담에 사용할 약품 후보가 없습니다. 약을 등록한 뒤 다시 질문해주세요.",
                    List.of(),
                    true);
        }

        try {
            AiChatResponseDto response = pythonAiClient.chat(AiChatRequestDto.builder()
                    .question(question)
                    .identifiedPills(referencedPills)
                    .contextItems(contextItems)
                    .userProfile(userProfile)
                    .build());
            if (response == null || response.getAnswer() == null || response.getAnswer().isBlank()) {
                return fallbackAnswer(referencedPills);
            }
            return new AiAnswer(
                    response.getAnswer(),
                    response.getReferencedPills() != null ? response.getReferencedPills() : referencedPills,
                    false);
        } catch (RuntimeException e) {
            return fallbackAnswer(referencedPills);
        }
    }

    private AiAnswer fallbackAnswer(List<String> referencedPills) {
        return new AiAnswer(
                "AI 상담 서버 연결에 실패했습니다. 현재 등록된 약 목록은 "
                        + String.join(", ", referencedPills)
                        + "입니다. 복용 변경 전 의사 또는 약사와 상담하세요.",
                referencedPills,
                true);
    }

    private boolean isCabinetListQuestion(String question) {
        String normalized = question.replaceAll("\\s+", "").toLowerCase();
        return (normalized.contains("약장") || normalized.contains("등록된약") || normalized.contains("복용중"))
                && (normalized.contains("목록")
                || normalized.contains("리스트")
                || normalized.contains("뭐")
                || normalized.contains("무엇")
                || normalized.contains("알려")
                || normalized.contains("보여")
                || normalized.contains("조회"));
    }

    private AiAnswer answerCabinetList(List<String> referencedPills) {
        if (referencedPills.isEmpty()) {
            return new AiAnswer("내 약장에 등록된 약품이나 영양제가 없습니다.", List.of(), false);
        }
        return new AiAnswer(
                "내 약장에는 " + String.join(", ", referencedPills) + "이(가) 등록되어 있습니다.",
                referencedPills,
                false);
    }

    private CabinetContext resolveCabinetContext(User user, ChatQuestionRequestDto requestDto) {
        if (requestDto.getUseMyCabinet() == null || requestDto.getUseMyCabinet()) {
            List<UserMedicationReg> registrations = userMedicationRegRepository.findByUserId(user.getId());
            List<String> names = resolveCabinetItemNames(registrations);
            return new CabinetContext(names, buildContextItems(user, registrations), buildUserProfile(user));
        }
        List<String> identifiedPills = requestDto.getIdentifiedPills() == null ? List.of() : requestDto.getIdentifiedPills();
        return new CabinetContext(identifiedPills, List.of(), buildUserProfile(user));
    }

    private List<String> resolveCabinetItemNames(List<UserMedicationReg> registrations) {
        List<String> names = new ArrayList<>();
        for (UserMedicationReg reg : registrations) {
            if (reg.getItem_type() == ItemType.MEDICINE) {
                medicineRepository.findById(reg.getItemId())
                        .map(MedicineMaster::getMedicineName)
                        .ifPresent(names::add);
            } else if (reg.getItem_type() == ItemType.SUPPLEMENT) {
                supplementRepository.findById(reg.getItemId())
                        .map(SupplementMaster::getSupplementName)
                        .ifPresent(names::add);
            }
        }
        return names;
    }

    private List<Map<String, Object>> buildContextItems(User user, List<UserMedicationReg> registrations) {
        Map<Long, List<String>> intakeTimesByRegId = intakeScheduleRepository.findSchedulesForUser(user.getId()).stream()
                .collect(Collectors.groupingBy(
                        schedule -> schedule.getUserMedicationReg().getId(),
                        Collectors.mapping(schedule -> schedule.getTimeSlot(), Collectors.toList())
                ));

        List<Map<String, Object>> items = new ArrayList<>();
        for (UserMedicationReg reg : registrations) {
            if (reg.getItem_type() == ItemType.MEDICINE) {
                medicineRepository.findById(reg.getItemId()).ifPresent(medicine -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("itemName", medicine.getMedicineName());
                    item.put("itemType", ItemType.MEDICINE.name());
                    item.put("manufacturer", medicine.getMedicineManufacturer());
                    item.put("ingredients", medicine.getIngredients().stream()
                            .map(ingredient -> Map.<String, Object>of(
                                    "name", ingredient.getIngredientMaster().getIngredientName(),
                                    "dosage", ingredient.getDosage() != null ? ingredient.getDosage().toPlainString() : ""
                            ))
                            .toList());
                    item.put("intakeTimes", intakeTimesByRegId.getOrDefault(reg.getId(), List.of()));
                    item.put("efficacy", medicine.getEfficacy());
                    item.put("precautions", medicine.getPrecautions());
                    items.add(item);
                });
            } else if (reg.getItem_type() == ItemType.SUPPLEMENT) {
                supplementRepository.findById(reg.getItemId()).ifPresent(supplement -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("itemName", supplement.getSupplementName());
                    item.put("itemType", ItemType.SUPPLEMENT.name());
                    item.put("manufacturer", supplement.getSupplementManufacturer());
                    item.put("ingredients", supplement.getIngredients().stream()
                            .map(ingredient -> Map.<String, Object>of(
                                    "name", ingredient.getIngredientMaster().getIngredientName(),
                                    "dosage", ingredient.getDosage() != null ? ingredient.getDosage().toPlainString() : ""
                            ))
                            .toList());
                    item.put("intakeTimes", intakeTimesByRegId.getOrDefault(reg.getId(), List.of()));
                    item.put("efficacy", supplement.getEfficacy());
                    item.put("precautions", supplement.getPrecautions());
                    items.add(item);
                });
            }
        }
        return items;
    }

    private Map<String, Object> buildUserProfile(User user) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("gender", user.getGender() != null ? user.getGender().name() : null);
        profile.put("birthDate", user.getBirthDate() != null ? user.getBirthDate().toString() : null);
        healthProfileRepository.findByUserId(user.getId()).ifPresent(healthProfile -> {
            profile.put("disease", healthProfile.getDisease());
            profile.put("allergy", healthProfile.getAllergy());
        });
        return profile;
    }

    private ChatMessage saveMessage(ChatSession session, SenderRole senderRole, String contents) {
        ChatMessage message = new ChatMessage();
        message.setChatSession(session);
        message.setSender_role(senderRole);
        message.setContents(contents);
        return chatMessageRepository.save(message);
    }

    private ChatSession getOwnedSession(Long sessionId, User user) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅 세션입니다."));
        if (!session.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("해당 채팅 세션에 접근할 권한이 없습니다.");
        }
        return session;
    }

    private User getUser(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private ChatSessionResponseDto toSessionResponse(ChatSession session) {
        return ChatSessionResponseDto.builder()
                .sessionId(session.getId())
                .startedAt(session.getStartedAt())
                .createdAt(session.getCreatedAt())
                .build();
    }

    private ChatMessageResponseDto toMessageResponse(ChatMessage message) {
        return ChatMessageResponseDto.builder()
                .messageId(message.getId())
                .sessionId(message.getChatSession().getId())
                .senderRole(message.getSender_role())
                .contents(message.getContents())
                .createdAt(message.getCreatedAt())
                .build();
    }

    private record AiAnswer(String answer, List<String> referencedPills, boolean fallback) {
    }

    private record CabinetContext(
            List<String> referencedPills,
            List<Map<String, Object>> contextItems,
            Map<String, Object> userProfile
    ) {
    }
}
