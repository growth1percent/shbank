package com.example.shbank.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
// 1회 이체 한도 조회 및 변경 응답
public class AccountSettingResponse {
    private Long accountId;
    private Integer transferLimit;
}
