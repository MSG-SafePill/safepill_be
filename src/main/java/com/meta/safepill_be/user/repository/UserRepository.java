package com.meta.safepill_be.user.repository;

import com.meta.safepill_be.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByLoginId(String loginId); // 아이디로 회원 찾기
    boolean existsByLoginId(String loginId);      // 아이디 중복 확인
}