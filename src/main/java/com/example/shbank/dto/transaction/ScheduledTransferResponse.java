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
public class ScheduledTransferResponse {
    private Long accountId;       // 계좌 ID
    private Long transferId;      // Transaction ID
    private String recipientName; // 수취인 이름
    private int amount;           // 금액
    private LocalDateTime scheduleDate; // 예약 송금일
    private String memo;          // 메모
}
