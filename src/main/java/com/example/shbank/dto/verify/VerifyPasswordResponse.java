package com.example.shbank.dto.verify;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
// 계좌 비밀번호 인증 응답
public class VerifyPasswordResponse {
    private Boolean verified;
}
