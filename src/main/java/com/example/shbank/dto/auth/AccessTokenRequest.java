package com.example.shbank.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
// 액세스 토큰 갱신 요청
public class AccessTokenRequest {
    private String refreshToken;
}
