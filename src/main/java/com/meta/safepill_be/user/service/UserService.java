package com.meta.safepill_be.user.service;

import com.meta.safepill_be.user.domain.Provider;
import com.meta.safepill_be.user.domain.User;
import com.meta.safepill_be.user.dto.LoginRequestDto;
import com.meta.safepill_be.user.dto.SignupRequestDto;
import com.meta.safepill_be.user.repository.UserRepository;
import com.meta.safepill_be.user.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    // 1. 회원가입 로직
    @Transactional
    public String signup(SignupRequestDto requestDto) {
        // 아이디 중복 체크
        if (userRepository.existsByLoginId(requestDto.getLoginId())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        User user = new User();
        user.setLoginId(requestDto.getLoginId());
        user.setPassword(requestDto.getPassword()); // 🚨 현재는 평문 저장 (추후 암호화 적용 필요)
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
        if (!user.getPassword().equals(requestDto.getPassword())) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }

        // 👇 성공 시 토큰 발급으로 변경!
        String token = jwtUtil.createToken(user.getLoginId());
        return token; // "로그인 성공!" 텍스트 대신 암호화된 토큰을 프론트엔드로 던져줍니다.
    }
}