package com.example.shbank.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
// 거래 요청 응답
public class TransactionResponse {
    private Long transactionId;
    private String senderName;
    private String senderAccount;
    private String recipientName;
    private String recipientAccount;
    private String type;
    private Integer amount;
    private Integer balance;
    private LocalDateTime transactionDate;
}
