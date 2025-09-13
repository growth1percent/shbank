package com.example.shbank.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLoginResponse {
    private String accessToken;
    private String refreshToken;
    private Boolean success;
    private Long userId;
    private String userName;
}
