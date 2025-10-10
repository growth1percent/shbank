package com.example.shbank.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
// 액세스 토큰 응답
public class AccessTokenResponse {
    private String accessToken;
    private int accessTokenExpiresIn;
    private String refreshToken;
    private int refreshTokenExpiresIn;
}
