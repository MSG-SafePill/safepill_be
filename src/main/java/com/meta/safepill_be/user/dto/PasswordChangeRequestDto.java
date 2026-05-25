package com.meta.safepill_be.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PasswordChangeRequestDto {
    private String currentPassword;
    private String newPassword;
}
