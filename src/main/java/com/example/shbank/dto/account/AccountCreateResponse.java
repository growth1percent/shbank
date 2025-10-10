package com.example.shbank.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
// 계좌 개설 응답
public class AccountCreateResponse {
    private Long accountId;
    private String accountName;
    private String accountNumber;
    private String accountType;
    private Integer balance;
    private Integer transferLimit;
}
