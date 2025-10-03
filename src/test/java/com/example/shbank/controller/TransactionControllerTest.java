package com.example.shbank.controller;

import com.example.shbank.dto.transaction.ScheduledTransferResponse;
import com.example.shbank.dto.transaction.TransactionHistoryResponse;
import com.example.shbank.dto.transaction.TransactionResponse;
import com.example.shbank.enums.TransactionType;
import com.example.shbank.exception.account.AccountNotFoundException;
import com.example.shbank.exception.transaction.InsufficientBalanceException;
import com.example.shbank.security.CustomUserDetails;
import com.example.shbank.service.TransactionService;
import com.example.shbank.util.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private JWTUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomUserDetails userDetails;

    @BeforeEach
    void setupAuthentication() {
        userDetails = new CustomUserDetails(
                new com.example.shbank.entity.User(1L, "test@example.com", "홍길동", "123456")
        );
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testGetTransactionHistory_Success() throws Exception {
        TransactionResponse tx = TransactionResponse.builder()
                .transactionId(100L)
                .senderName("홍길동")
                .recipientName("김철수")
                .amount(5000)
                .balance(50000)
                .type("TRANSFER_OUT")
                .transactionDate(LocalDateTime.now())
                .build();

        TransactionHistoryResponse response = TransactionHistoryResponse.builder()
                .totalIn(0)
                .totalOut(5000)
                .netChange(-5000)
                .transactions(List.of(tx))
                .build();

        Mockito.when(transactionService.getTransactionHistory(eq(1L), eq(userDetails.getUserId()),
                        eq(null), any(), any()))
                .thenReturn(response);

        mockMvc.perform(get("/api/transactions/{accountId}", 1L)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOut").value(5000))
                .andExpect(jsonPath("$.transactions[0].recipientName").value("김철수"));
    }

    @Test
    void testGetScheduledTransfers_Success() throws Exception {
        ScheduledTransferResponse scheduled = ScheduledTransferResponse.builder()
                .accountId(1L)
                .transferId(200L)
                .recipientName("김영희")
                .amount(10000)
                .scheduleDate(LocalDateTime.now().plusDays(1))
                .memo("용돈")
                .build();

        Mockito.when(transactionService.getScheduledTransfers(eq(1L), eq(userDetails.getUserId())))
                .thenReturn(List.of(scheduled));

        mockMvc.perform(get("/api/transactions/{accountId}/scheduled", 1L)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].recipientName").value("김영희"))
                .andExpect(jsonPath("$[0].amount").value(10000));
    }

    @Test
    void testTransfer_Success() throws Exception {
        TransactionResponse response = TransactionResponse.builder()
                .transactionId(300L)
                .senderName("홍길동")
                .recipientName("이순신")
                .amount(20000)
                .balance(80000)
                .type("TRANSFER_OUT")
                .transactionDate(LocalDateTime.now())
                .build();

        Mockito.when(transactionService.transfer(eq(1L), eq("1234-567-890123"),
                        eq(20000), any(), eq("메모"), eq(TransactionType.TRANSFER_OUT), eq(userDetails.getUserId())))
                .thenReturn(response);

        mockMvc.perform(post("/api/transactions/transfer")
                        .param("senderAccountId", "1")
                        .param("recipientAccountNumber", "1234-567-890123")
                        .param("amount", "20000")
                        .param("memo", "메모")
                        .param("type", "TRANSFER_OUT")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipientName").value("이순신"))
                .andExpect(jsonPath("$.amount").value(20000));
    }

    @Test
    void testTransfer_InsufficientBalance() throws Exception {
        Mockito.when(transactionService.transfer(eq(1L), eq("1234-567-890123"),
                        eq(1000000), any(), any(), eq(TransactionType.TRANSFER_OUT), eq(userDetails.getUserId())))
                .thenThrow(new InsufficientBalanceException("잔액이 부족합니다."));

        mockMvc.perform(post("/api/transactions/transfer")
                        .param("senderAccountId", "1")
                        .param("recipientAccountNumber", "1234-567-890123")
                        .param("amount", "1000000")
                        .param("type", "TRANSFER_OUT")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testTransfer_AccountNotFound() throws Exception {
        Mockito.when(transactionService.transfer(eq(99L), eq("1234-567-890123"),
                        eq(1000), any(), any(), eq(TransactionType.TRANSFER_OUT), eq(userDetails.getUserId())))
                .thenThrow(new AccountNotFoundException("송금 계좌가 존재하지 않습니다."));

        mockMvc.perform(post("/api/transactions/transfer")
                        .param("senderAccountId", "99")
                        .param("recipientAccountNumber", "1234-567-890123")
                        .param("amount", "1000")
                        .param("type", "TRANSFER_OUT")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCancelScheduledTransfer_Success() throws Exception {
        doNothing().when(transactionService).cancelScheduledTransfer(eq(400L), eq(userDetails.getUserId()));

        mockMvc.perform(patch("/api/transactions/scheduled/{transactionId}/cancel", 400L)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void testCancelScheduledTransfer_NotScheduled() throws Exception {
        Mockito.doThrow(new IllegalStateException("예약 송금만 취소할 수 있습니다."))
                .when(transactionService).cancelScheduledTransfer(eq(400L), eq(userDetails.getUserId()));

        mockMvc.perform(patch("/api/transactions/scheduled/{transactionId}/cancel", 400L)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCardPayment_Success() throws Exception {
        TransactionResponse response = TransactionResponse.builder()
                .transactionId(500L)
                .senderName("홍길동")
                .recipientName("스타벅스")
                .amount(7000)
                .balance(93000)
                .type("TRANSFER_OUT")
                .transactionDate(LocalDateTime.now())
                .build();

        Mockito.when(transactionService.cardPayment(eq(1L), eq(7000), eq("스타벅스")))
                .thenReturn(response);

        mockMvc.perform(post("/api/transactions/card-payment")
                        .param("accountId", "1")
                        .param("amount", "7000")
                        .param("merchantName", "스타벅스")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipientName").value("스타벅스"))
                .andExpect(jsonPath("$.amount").value(7000));
    }

    @Test
    void testCardPayment_InsufficientBalance() throws Exception {
        Mockito.when(transactionService.cardPayment(eq(1L), eq(1000000), eq("스타벅스")))
                .thenThrow(new InsufficientBalanceException("잔액이 부족합니다."));

        mockMvc.perform(post("/api/transactions/card-payment")
                        .param("accountId", "1")
                        .param("amount", "1000000")
                        .param("merchantName", "스타벅스")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}
