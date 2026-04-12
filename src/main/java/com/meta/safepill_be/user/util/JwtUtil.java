package com.meta.safepill_be.user.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expiration;

    // application.properties에 적어둔 비밀키와 만료시간을 가져옵니다.
    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration}") long expiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    // 아이디를 넣으면 암호화된 토큰을 뱉어내는 메서드
    public String createToken(String loginId) {
        return Jwts.builder()
                .subject(loginId) // 출입증에 유저 아이디 새기기
                .issuedAt(new Date()) // 발급 시간
                .expiration(new Date(System.currentTimeMillis() + expiration)) // 만료 시간 (1시간 뒤)
                .signWith(secretKey) // 우리 서버만의 비밀 도장 쾅!
                .compact();
    }
}