package com.example.shbank.dto.verify;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
// 계좌 비밀번호 인증 요청
public class VerifyPasswordRequest {
    @Pattern(regexp = "\\d{6}", message = "6자리 숫자여야 합니다.")
    private String authPassword;
}
