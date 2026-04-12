package com.meta.safepill_be.user.controller;

import com.meta.safepill_be.user.dto.LoginRequestDto;
import com.meta.safepill_be.user.dto.SignupRequestDto;
import com.meta.safepill_be.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*") //(모든 웹 브라우저의 접근을 허락함)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입 API (POST /api/users/signup)
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequestDto requestDto) {
        String result = userService.signup(requestDto);
        return ResponseEntity.ok(result);
    }

    // 로그인 API (POST /api/users/login)
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDto requestDto) {
        String result = userService.login(requestDto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/check-id")
    public ResponseEntity<String> checkId(@RequestParam("loginId") String loginId) {
        // 이미 가입된 아이디인지 검사 (UserService에 있는 기능 재활용!)
        if (userService.checkIdDuplication(loginId)) {
            return ResponseEntity.badRequest().body("이미 사용 중인 아이디입니다.");
        }
        return ResponseEntity.ok("사용 가능한 아이디입니다.");
    }
}