package com.meta.safepill_be.user.controller;

import com.meta.safepill_be.user.dto.LoginRequestDto;
import com.meta.safepill_be.user.dto.GoogleLoginRequestDto;
import com.meta.safepill_be.user.dto.PasswordChangeRequestDto;
import com.meta.safepill_be.user.dto.SignupRequestDto;
import com.meta.safepill_be.user.dto.UserProfileResponseDto;
import com.meta.safepill_be.user.dto.UserProfileUpdateRequestDto;
import com.meta.safepill_be.user.service.UserService;
import com.meta.safepill_be.user.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*") //(모든 웹 브라우저의 접근을 허락함)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

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

    @PostMapping("/oauth/google")
    public ResponseEntity<String> googleLogin(@RequestBody GoogleLoginRequestDto requestDto) {
        return ResponseEntity.ok(userService.loginWithGoogle(requestDto));
    }

    @GetMapping("/check-id")
    public ResponseEntity<String> checkId(@RequestParam("loginId") String loginId) {
        // 이미 가입된 아이디인지 검사 (UserService에 있는 기능 재활용!)
        if (userService.checkIdDuplication(loginId)) {
            return ResponseEntity.badRequest().body("이미 사용 중인 아이디입니다.");
        }
        return ResponseEntity.ok("사용 가능한 아이디입니다.");
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDto> getProfile(
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(userService.getProfile(extractLoginId(token)));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserProfileResponseDto> updateProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody UserProfileUpdateRequestDto requestDto) {
        return ResponseEntity.ok(userService.updateProfile(extractLoginId(token), requestDto));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<String> changePassword(
            @RequestHeader("Authorization") String token,
            @RequestBody PasswordChangeRequestDto requestDto) {
        return ResponseEntity.ok(userService.changePassword(extractLoginId(token), requestDto));
    }

    private String extractLoginId(String token) {
        String actualToken = token.replace("Bearer ", "");
        return jwtUtil.getLoginIdFromToken(actualToken);
    }
}
