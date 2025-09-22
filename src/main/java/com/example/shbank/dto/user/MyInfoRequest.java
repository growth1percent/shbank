package com.example.shbank.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MyInfoRequest {
    private String name;

    @Email
    @NotBlank
    private String email;

    private Password password;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Password {
        @NotBlank
        private String current;

        @NotBlank
        private String newPassword;
    }
}
