package com.example.shbank.controller;

import com.example.shbank.dto.transaction.*;
import com.example.shbank.enums.TransactionType;
import com.example.shbank.security.CustomUserDetails;
import com.example.shbank.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // 거래 내역 조회
    @GetMapping("/{accountId}")
    public ResponseEntity<TransactionHistoryResponse> getTransactionHistory(
            @PathVariable Long accountId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            Authentication authentication) {

        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();

        TransactionHistoryResponse response = transactionService.getTransactionHistory(accountId, userId, type, start, end);
        return ResponseEntity.ok(response);
    }

    // 예약 송금 목록 조회
    @GetMapping("/{accountId}/scheduled")
    public ResponseEntity<List<ScheduledTransferResponse>> getScheduledTransfers(
            @PathVariable Long accountId,
            Authentication authentication) {

        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();

        List<ScheduledTransferResponse> response = transactionService.getScheduledTransfers(accountId, userId);
        return ResponseEntity.ok(response);
    }

    // 즉시/예약 송금
    @PostMapping("/transfer")
    public ResponseEntity<Object> transfer(
            @Valid @RequestBody TransactionRequest request,
            Authentication authentication) {

        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();

        Object response = transactionService.transfer(
                request.getSenderAccountId(),
                request.getRecipientAccountNumber(),
                request.getAmount(),
                request.getScheduleDate(),
                request.getMemo(),
                request.getType(),
                userId
        );

        return ResponseEntity.ok(response);
    }

    // 예약 송금 취소
    @PatchMapping("/scheduled/{transactionId}/cancel")
    public ResponseEntity<Void> cancelScheduledTransfer(
            @PathVariable Long transactionId,
            Authentication authentication) {

        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();

        transactionService.cancelScheduledTransfer(transactionId, userId);
        return ResponseEntity.ok().build();
    }

    // 카드 결제
    @PostMapping("/card-payment")
    public ResponseEntity<CardPaymentResponse> cardPayment(
            @Valid @RequestBody CardPaymentRequest request,
            Authentication authentication) {

        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();

        CardPaymentResponse response = transactionService.cardPayment(
                request.getAccountId(),
                request.getAmount(),
                request.getMerchantName()
        );

        return ResponseEntity.ok(response);
    }
}
