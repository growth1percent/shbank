package com.example.shbank.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
// 내 정보 수정 응답
public class MyInfoUpdateResponse {
    private String name;
    private String email;
}
