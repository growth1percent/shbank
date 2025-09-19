package com.example.shbank.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
// 내 정보 응답
public class MyInfoResponse {
    private Long userId;
    private String name;
    private String email;
}
