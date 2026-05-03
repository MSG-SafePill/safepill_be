package com.meta.safepill_be.cabinet.repository;

import com.meta.safepill_be.cabinet.domain.ItemType;
import com.meta.safepill_be.cabinet.domain.UserMedicationReg;
import com.meta.safepill_be.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserMedicationRegRepository extends JpaRepository<UserMedicationReg, Long> {
    List<UserMedicationReg> findByUserId(Long userId);
    @Query("SELECT COUNT(u) > 0 FROM UserMedicationReg u WHERE u.user = :user AND u.item_type = :itemType AND u.itemId = :itemId")
    boolean isAlreadyRegistered(@Param("user") User user,
                                @Param("itemType") ItemType itemType,
                                @Param("itemId") Long itemId);
}