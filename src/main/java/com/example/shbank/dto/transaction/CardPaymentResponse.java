package com.example.shbank.dto.transaction;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardPaymentResponse {
    private Long transactionId;
    private String senderName;
    private String senderAccount;
    private String merchantName;
    private String type;
    private Integer amount;
    private Integer balance;
    private LocalDateTime transactionDate;
}
