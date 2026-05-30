package com.meta.safepill_be.user.service;

import com.meta.safepill_be.user.domain.Provider;
import com.meta.safepill_be.user.domain.Gender;
import com.meta.safepill_be.user.domain.User;
import com.meta.safepill_be.user.dto.GoogleLoginRequestDto;
import com.meta.safepill_be.user.dto.LoginRequestDto;
import com.meta.safepill_be.user.dto.PasswordChangeRequestDto;
import com.meta.safepill_be.user.dto.SignupRequestDto;
import com.meta.safepill_be.user.dto.UserProfileResponseDto;
import com.meta.safepill_be.user.dto.UserProfileUpdateRequestDto;
import com.meta.safepill_be.user.repository.UserRepository;
import com.meta.safepill_be.user.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Value("${google.oauth.client-id:}")
    private String googleOAuthClientId;

    // 1. 회원가입 로직
    @Transactional
    public String signup(SignupRequestDto requestDto) {
        // 아이디 중복 체크
        if (userRepository.existsByLoginId(requestDto.getLoginId())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        User user = new User();
        user.setLoginId(requestDto.getLoginId());
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        user.setUsername(requestDto.getUsername());
        user.setEmail(requestDto.getEmail());
        user.setGender(requestDto.getGender());
        user.setBirthDate(requestDto.getBirthDate());

        // 로컬 가입이므로 Provider 설정 및 빈 socialId 채우기 (DB 제약조건 우회)
        user.setProvider(Provider.Local);
        user.setSocialId("LOCAL");

        userRepository.save(user);
        return "회원가입이 완료되었습니다.";
    }

    // 2. 로그인 로직
    public String login(LoginRequestDto requestDto) {
        // 아이디 확인
        User user = userRepository.findByLoginId(requestDto.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 아이디입니다."));

        // 비밀번호 확인
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }

        // 👇 성공 시 토큰 발급으로 변경!
        String token = jwtUtil.createToken(user.getLoginId());
        return token; // "로그인 성공!" 텍스트 대신 암호화된 토큰을 프론트엔드로 던져줍니다.
    }

    @Transactional
    public String loginWithGoogle(GoogleLoginRequestDto requestDto) {
        if (requestDto.getIdToken() == null || requestDto.getIdToken().isBlank()) {
            throw new IllegalArgumentException("Google ID 토큰은 필수입니다.");
        }
        if (googleOAuthClientId == null || googleOAuthClientId.isBlank()) {
            throw new IllegalStateException("Google OAuth Client ID가 설정되지 않았습니다.");
        }

        GoogleUserInfo googleUser = verifyGoogleIdToken(requestDto.getIdToken().trim());
        User user = userRepository.findByProviderAndSocialId(Provider.Google, googleUser.sub())
                .orElseGet(() -> createGoogleUser(googleUser));
        return jwtUtil.createToken(user.getLoginId());
    }

    private GoogleUserInfo verifyGoogleIdToken(String idToken) {
        Map<String, Object> tokenInfo;
        try {
            tokenInfo = WebClient.create()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("oauth2.googleapis.com")
                            .path("/tokeninfo")
                            .queryParam("id_token", idToken)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    })
                    .block();
        } catch (WebClientResponseException e) {
            throw new IllegalArgumentException("유효하지 않은 Google ID 토큰입니다.");
        }

        if (tokenInfo == null) {
            throw new IllegalArgumentException("Google ID 토큰을 검증할 수 없습니다.");
        }
        String audience = valueAsString(tokenInfo.get("aud"));
        if (!googleOAuthClientId.equals(audience)) {
            throw new IllegalArgumentException("Google ID 토큰의 클라이언트 ID가 일치하지 않습니다.");
        }
        String issuer = valueAsString(tokenInfo.get("iss"));
        if (!"accounts.google.com".equals(issuer) && !"https://accounts.google.com".equals(issuer)) {
            throw new IllegalArgumentException("Google ID 토큰 발급자가 올바르지 않습니다.");
        }
        String emailVerified = valueAsString(tokenInfo.get("email_verified"));
        if (!"true".equalsIgnoreCase(emailVerified)) {
            throw new IllegalArgumentException("Google 이메일 인증이 완료되지 않은 계정입니다.");
        }

        String sub = valueAsString(tokenInfo.get("sub"));
        String email = valueAsString(tokenInfo.get("email"));
        if (sub.isBlank() || email.isBlank()) {
            throw new IllegalArgumentException("Google 계정 정보를 확인할 수 없습니다.");
        }
        String name = valueAsString(tokenInfo.get("name"));
        return new GoogleUserInfo(sub, email, name.isBlank() ? email.split("@")[0] : name);
    }

    private User createGoogleUser(GoogleUserInfo googleUser) {
        User user = new User();
        user.setLoginId("google:" + googleUser.sub());
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setUsername(googleUser.name());
        user.setEmail(googleUser.email());
        user.setSocialId(googleUser.sub());
        user.setProvider(Provider.Google);
        user.setGender(Gender.MALE);
        user.setBirthDate(LocalDate.of(1970, 1, 1));
        return userRepository.save(user);
    }

    private String valueAsString(Object value) {
        return value == null ? "" : value.toString();
    }

    // 👇 UserService 안에 중복 여부만 리턴하는 메서드 추가!
    public boolean checkIdDuplication(String loginId) {
        return userRepository.existsByLoginId(loginId);
    }

    @Transactional(readOnly = true)
    public UserProfileResponseDto getProfile(String loginId) {
        User user = findUser(loginId);
        return toProfileResponse(user);
    }

    @Transactional
    public UserProfileResponseDto updateProfile(String loginId, UserProfileUpdateRequestDto requestDto) {
        User user = findUser(loginId);
        String username = requestDto.getUsername() == null ? "" : requestDto.getUsername().trim();
        if (username.isEmpty()) {
            throw new IllegalArgumentException("닉네임은 필수입니다.");
        }
        if (username.length() > 30) {
            throw new IllegalArgumentException("닉네임은 30자 이하로 입력해주세요.");
        }
        user.setUsername(username);
        return toProfileResponse(user);
    }

    @Transactional
    public String changePassword(String loginId, PasswordChangeRequestDto requestDto) {
        User user = findUser(loginId);
        if (user.getProvider() != Provider.Local) {
            throw new IllegalArgumentException("소셜 로그인 계정은 비밀번호를 변경할 수 없습니다.");
        }

        String currentPassword = requestDto.getCurrentPassword();
        String newPassword = requestDto.getNewPassword();
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new IllegalArgumentException("현재 비밀번호는 필수입니다.");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("새 비밀번호는 필수입니다.");
        }
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("새 비밀번호는 8자 이상이어야 합니다.");
        }
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        return "비밀번호가 변경되었습니다.";
    }

    private User findUser(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private UserProfileResponseDto toProfileResponse(User user) {
        return UserProfileResponseDto.builder()
                .id(user.getId())
                .loginId(user.getLoginId())
                .username(user.getUsername())
                .email(user.getEmail())
                .gender(user.getGender())
                .birthDate(user.getBirthDate())
                .provider(user.getProvider())
                .build();
    }

    private record GoogleUserInfo(String sub, String email, String name) {
    }
}
