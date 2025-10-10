package com.example.shbank.controller;

import com.example.shbank.dto.account.AccountCreateRequest;
import com.example.shbank.dto.account.AccountCreateResponse;
import com.example.shbank.dto.account.AccountResponse;
import com.example.shbank.dto.account.AccountSettingRequest;
import com.example.shbank.dto.verify.VerifyPasswordRequest;
import com.example.shbank.entity.Account;
import com.example.shbank.security.CustomUserDetails;
import com.example.shbank.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    // 계좌 개설
    @PostMapping
    public ResponseEntity<AccountCreateResponse> createAccount(@RequestBody @Valid AccountCreateRequest request,
                                                               Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();
        AccountCreateResponse response = accountService.createAccount(request, userId);

        return ResponseEntity.status(201).body(response);
    }

    // 계좌 목록 조회
    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAccounts(Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();
        List<AccountResponse> accounts = accountService.getAccountByUser(userId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(accounts);
    }

    // 계좌 번호 조회
    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccountByAccountNumber(@PathVariable String accountNumber) {
        Account account = accountService.getAccountByAccountNumber(accountNumber);

        AccountResponse response = new AccountResponse(
                account.getId(),
                account.getAccountName(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getType().name(),
                account.getTransferLimit()
        );

        return ResponseEntity.ok(response);
    }

    // 계좌 설정 변경
    @PatchMapping("/{accountId}/settings")
    public ResponseEntity<Void> updateAccountSettings(@PathVariable Long accountId,
                                      @RequestBody @Valid AccountSettingRequest request,
                                      Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();
        accountService.updateAccountSettings(accountId, userId, request);

        return ResponseEntity.ok().build();
    }

    // 계좌 설정 조회
    @GetMapping("/{accountId}/settings")
    public ResponseEntity<Integer> getTransferLimit(@PathVariable Long accountId,
                                    Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();
        Integer limit = accountService.getTransferLimit(accountId, userId);
        return ResponseEntity.ok(limit);
    }

    // 계좌 인증 비밀번호 확인
    @PostMapping("/{accountId}/verify-password")
    public ResponseEntity<Boolean> verifyAuthPassword(@PathVariable Long accountId,
                                      @Valid @RequestBody VerifyPasswordRequest request) {
        boolean result = accountService.verifyAuthPassword(accountId, request.getAuthPassword());
        return ResponseEntity.ok(result);
    }
}