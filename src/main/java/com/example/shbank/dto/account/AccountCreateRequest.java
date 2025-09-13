package com.example.shbank.dto.account;

import com.example.shbank.enums.AccountType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
// 계좌 개설 요청
public class AccountCreateRequest {
    @NotBlank
    private String accountName;

    @NotNull
    private AccountType accountType;

    @NotNull
    @Min(0)
    private Integer initialAmount;

    @NotNull
    @Min(0)
    private Integer transferLimit;

    @NotNull
    @Pattern(regexp = "\\d{6}", message = "6자리 숫자여야 합니다.")
    private String authPassword;
}
