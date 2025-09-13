package com.example.shbank.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
// 거래 내역 응답
public class TransactionHistoryResponse {
    private Integer totalIn;
    private Integer totalOut;
    private Integer netChange;
    private List<TransactionResponse> transactions;
}
