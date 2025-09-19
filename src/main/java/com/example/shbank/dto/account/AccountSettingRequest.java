package com.example.shbank.dto.account;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountSettingRequest {
    private Integer transferLimit;
    private AuthPassword authPassword;

    private static class AuthPassword {
        @Pattern(regexp = "\\d{6}", message = "6자리 숫자여야 합니다.")
        private String current;
        @Pattern(regexp = "\\d{6}", message = "6자리 숫자여야 합니다.")
        private String newPassword;
    }
}
