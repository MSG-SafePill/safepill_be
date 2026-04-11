package com.meta.safepill_be.user.controller;

import com.meta.safepill_be.user.dto.LoginRequestDto;
import com.meta.safepill_be.user.dto.SignupRequestDto;
import com.meta.safepill_be.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
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
}