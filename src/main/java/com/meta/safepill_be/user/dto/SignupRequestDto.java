package com.meta.safepill_be.user.dto;

import com.meta.safepill_be.user.domain.Gender;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SignupRequestDto {
    private String loginId;
    private String password;
    private String username;
    private String email;
    private Gender gender;
    private LocalDate birthDate;
}