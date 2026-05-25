package com.meta.safepill_be.user.dto;

import com.meta.safepill_be.user.domain.Gender;
import com.meta.safepill_be.user.domain.Provider;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class UserProfileResponseDto {
    private Long id;
    private String loginId;
    private String username;
    private String email;
    private Gender gender;
    private LocalDate birthDate;
    private Provider provider;
}
