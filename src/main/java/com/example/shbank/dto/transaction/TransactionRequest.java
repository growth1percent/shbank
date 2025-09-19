package com.example.shbank.dto.transaction;

import com.example.shbank.enums.TransactionType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
// 거래 요청 (이체, 카드 결제)
public class TransactionRequest {
    @NotBlank
    private String recipientAccountNumber;

    @NotNull
    private Integer amount;

    @NotNull
    private TransactionType type;

    @Future
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduleDate;

    private String memo;
}
