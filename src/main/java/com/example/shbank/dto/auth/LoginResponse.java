package com.example.shbank.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
// 로그인 응답
public class LoginResponse {
    private String accessToken;
    private int accessTokenExpiresIn;
    private String refreshToken;
    private int refreshTokenExpiresIn;
    private Boolean success;
    private Long userId;
    private String userName;
    private String email;
}
