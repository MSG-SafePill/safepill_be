package com.meta.safepill_be.user.repository;

import com.meta.safepill_be.user.domain.HealthProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HealthProfileRepository extends JpaRepository<HealthProfile, Long> {
    Optional<HealthProfile> findByUserId(Long userId);
}