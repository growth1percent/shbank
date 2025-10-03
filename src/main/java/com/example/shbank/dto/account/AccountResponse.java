package com.example.shbank.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
// 계좌 조회 응답
public class AccountResponse {
    private Long accountId;
    private String accountName;
    private String accountNumber;
    private Integer balance;
    private String accountType;
    private Integer transferLimit;
}
