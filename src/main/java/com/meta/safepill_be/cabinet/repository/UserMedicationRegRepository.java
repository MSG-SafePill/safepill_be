package com.meta.safepill_be.cabinet.repository;

import com.meta.safepill_be.cabinet.domain.UserMedicationReg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserMedicationRegRepository extends JpaRepository<UserMedicationReg, Long> {
    List<UserMedicationReg> findByUserId(Long userId);
}