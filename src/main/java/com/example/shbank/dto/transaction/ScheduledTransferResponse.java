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
    private Long accountId;
    private Long transferId;
    private String recipientName;
    private int amount;
    private int balance;
    private String type;
    private LocalDateTime scheduleDate;
    private String memo;
}
