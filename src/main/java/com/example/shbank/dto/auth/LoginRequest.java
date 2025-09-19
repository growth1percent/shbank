package com.example.shbank.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
// 로그인 요청 (토큰 발급)
public class LoginRequest {
    @Email
    @NotBlank
    private String email;


    @NotBlank
    @Pattern(regexp = "\\d{6}", message = "6자리 숫자여야 합니다.")
    private String password;
}
