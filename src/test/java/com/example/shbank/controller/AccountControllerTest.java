package com.example.shbank.controller;

import com.example.shbank.dto.account.*;
import com.example.shbank.dto.verify.VerifyPasswordRequest;
import com.example.shbank.entity.Account;
import com.example.shbank.enums.AccountType;
import com.example.shbank.exception.account.AccountNotFoundException;
import com.example.shbank.security.CustomUserDetails;
import com.example.shbank.service.AccountService;
import com.example.shbank.util.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private JWTUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomUserDetails userDetails;

    @BeforeEach
    void setupAuthentication() {
        userDetails = new CustomUserDetails(
                new com.example.shbank.entity.User(1L, "test@example.com", "홍길동", "password")
        );

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testCreateAccount_Success() throws Exception {
        AccountCreateRequest request = AccountCreateRequest.builder()
                .accountName("Test Account")
                .accountType(AccountType.SAVINGS)
                .initialAmount(10000)
                .transferLimit(5000)
                .authPassword("123456")
                .build();

        AccountCreateResponse response = AccountCreateResponse.builder()
                .accountId(1L)
                .accountName("Test Account")
                .accountNumber("1234-567-890123")
                .accountType("SAVINGS")
                .balance(10000)
                .transferLimit(5000)
                .build();

        Mockito.when(accountService.createAccount(any(AccountCreateRequest.class), eq(userDetails.getUserId())))
                .thenReturn(response);

        mockMvc.perform(post("/api/accounts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value(response.getAccountId()))
                .andExpect(jsonPath("$.accountName").value(response.getAccountName()))
                .andExpect(jsonPath("$.accountNumber").value(response.getAccountNumber()))
                .andExpect(jsonPath("$.accountType").value(response.getAccountType()))
                .andExpect(jsonPath("$.balance").value(response.getBalance()))
                .andExpect(jsonPath("$.transferLimit").value(response.getTransferLimit()));
    }

    @Test
    void testCreateAccount_AccountNotFound() throws Exception {
        AccountCreateRequest request = AccountCreateRequest.builder()
                .accountName("Test Account")
                .accountType(AccountType.SAVINGS)
                .initialAmount(10000)
                .transferLimit(5000)
                .authPassword("123456")
                .build();

        Mockito.when(accountService.createAccount(any(AccountCreateRequest.class), eq(userDetails.getUserId())))
                .thenThrow(new AccountNotFoundException("해당 계좌가 존재하지 않습니다."));

        mockMvc.perform(post("/api/accounts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("해당 계좌가 존재하지 않습니다."));
    }

    @Test
    void testGetAccounts_Success() throws Exception {
        List<AccountResponse> accounts = List.of(
                new AccountResponse(1L, "Account", "1234-567-890123", 10000, "SAVINGS",5000)
        );

        Mockito.when(accountService.getAccountByUser(eq(userDetails.getUserId())))
                .thenReturn(accounts);

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountId").value(accounts.get(0).getAccountId()))
                .andExpect(jsonPath("$[0].accountName").value(accounts.get(0).getAccountName()))
                .andExpect(jsonPath("$[0].accountNumber").value(accounts.get(0).getAccountNumber()))
                .andExpect(jsonPath("$[0].accountType").value(accounts.get(0).getAccountType()))
                .andExpect(jsonPath("$[0].balance").value(accounts.get(0).getBalance()));
    }

    @Test
    void testGetAccountByAccountNumber_Success() throws Exception {
        Account account = Account.builder()
                .id(2L)
                .accountNumber("1234-567-890124")
                .balance(5000)
                .accountName("상대계좌")
                .type(AccountType.SAVINGS)
                .build();

        Mockito.when(accountService.getAccountByAccountNumber("1234-567-890124"))
                .thenReturn(account);

        mockMvc.perform(get("/api/accounts/1234-567-890124"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(2))
                .andExpect(jsonPath("$.accountNumber").value("1234-567-890124"))
                .andExpect(jsonPath("$.accountName").value("상대계좌"))
                .andExpect(jsonPath("$.balance").value(5000))
                .andExpect(jsonPath("$.accountType").value("SAVINGS"));
    }

    @Test
    void testGetAccountByAccountNumber_NotFound() throws Exception {
        Mockito.when(accountService.getAccountByAccountNumber("1234-567-890999"))
                .thenThrow(new AccountNotFoundException("해당 계좌를 찾을 수 없습니다."));

        mockMvc.perform(get("/api/accounts/1234-567-890999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateAccountSettings_Success() throws Exception {
        AccountSettingRequest request = new AccountSettingRequest();
        request.setTransferLimit(10000);

        mockMvc.perform(patch("/api/accounts/1/settings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Mockito.verify(accountService)
                .updateAccountSettings(eq(1L), eq(userDetails.getUserId()), any(AccountSettingRequest.class));
    }

    @Test
    void testUpdateAccountSettings_AccountNotFound() throws Exception {
        AccountSettingRequest request = new AccountSettingRequest();
        request.setTransferLimit(10000);

        Mockito.doThrow(new AccountNotFoundException("해당 계좌가 존재하지 않습니다."))
                .when(accountService)
                .updateAccountSettings(eq(1L), eq(userDetails.getUserId()), any(AccountSettingRequest.class));

        mockMvc.perform(patch("/api/accounts/1/settings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("해당 계좌가 존재하지 않습니다."));
    }

    @Test
    void testUpdateAccountSettings_AccessDenied() throws Exception {
        AccountSettingRequest request = new AccountSettingRequest();
        request.setTransferLimit(10000);

        Mockito.doThrow(new AccessDeniedException("해당 계좌에 접근할 권한이 없습니다."))
                .when(accountService)
                .updateAccountSettings(eq(1L), eq(userDetails.getUserId()), any(AccountSettingRequest.class));

        mockMvc.perform(patch("/api/accounts/1/settings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("해당 계좌에 접근할 권한이 없습니다."));
    }

    @Test
    void testGetTransferLimit_Success() throws Exception {
        Mockito.when(accountService.getTransferLimit(eq(1L), eq(userDetails.getUserId())))
                .thenReturn(5000);

        mockMvc.perform(get("/api/accounts/1/settings"))
                .andExpect(status().isOk())
                .andExpect(content().string("5000"));
    }

    @Test
    void testGetTransferLimit_AccountNotFound() throws Exception {
        Mockito.when(accountService.getTransferLimit(eq(1L), eq(userDetails.getUserId())))
                .thenThrow(new AccountNotFoundException("해당 계좌가 존재하지 않습니다."));

        mockMvc.perform(get("/api/accounts/1/settings"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("해당 계좌가 존재하지 않습니다."));
    }

    @Test
    void testGetTransferLimit_AccessDenied() throws Exception {
        Mockito.when(accountService.getTransferLimit(eq(1L), eq(userDetails.getUserId())))
                .thenThrow(new AccessDeniedException("해당 계좌에 접근할 권한이 없습니다."));

        mockMvc.perform(get("/api/accounts/1/settings"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("해당 계좌에 접근할 권한이 없습니다."));
    }

    @Test
    void testVerifyAuthPassword_Success() throws Exception {
        VerifyPasswordRequest request = VerifyPasswordRequest.builder()
                .authPassword("123456")
                .build();

        Mockito.when(accountService.verifyAuthPassword(eq(1L), eq("123456")))
                .thenReturn(true);

        mockMvc.perform(post("/api/accounts/1/verify-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void testVerifyAuthPassword_AccountNotFound() throws Exception {
        VerifyPasswordRequest request = VerifyPasswordRequest.builder()
                .authPassword("123456")
                .build();

        Mockito.when(accountService.verifyAuthPassword(eq(1L), eq("123456")))
                .thenThrow(new AccountNotFoundException("해당 계좌가 존재하지 않습니다."));

        mockMvc.perform(post("/api/accounts/1/verify-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("해당 계좌가 존재하지 않습니다."));
    }

    @Test
    void testVerifyAuthPassword_AccessDenied() throws Exception {
        VerifyPasswordRequest request = VerifyPasswordRequest.builder()
                .authPassword("123456")
                .build();

        Mockito.when(accountService.verifyAuthPassword(eq(1L), eq("123456")))
                .thenThrow(new AccessDeniedException("비밀번호가 일치하지 않습니다."));

        mockMvc.perform(post("/api/accounts/1/verify-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("비밀번호가 일치하지 않습니다."));
    }
}
