package com.example.shbank.controller;

import com.example.shbank.dto.account.AccountCreateRequest;
import com.example.shbank.dto.account.AccountResponse;
import com.example.shbank.dto.account.AccountSettingRequest;
import com.example.shbank.entity.User;
import com.example.shbank.security.CustomUserDetails;
import com.example.shbank.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public AccountResponse createAccount(@RequestBody @Valid AccountCreateRequest request,
                                         Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();
        return accountService.createAccount(request, userId);
    }

    // 계좌 목록 조회
    @GetMapping
    public List<AccountResponse> getAccounts(Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();
        return accountService.getAccountByUser(userId);
    }

    // 계좌 설정 변경
    @PatchMapping("/{accountId}/settings")
    public void updateAccountSettings(@PathVariable Long accountId,
                                      @RequestBody @Valid AccountSettingRequest request,
                                      Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();
        accountService.updateAccountSettings(accountId, userId, request);
    }

    // 계좌 설정 조회
    @GetMapping("/{accountId}/settings")
    public Integer getTransferLimit(@PathVariable Long accountId,
                                    Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();
        return accountService.getTransferLimit(accountId, userId);
    }

    // 계좌 인증 비밀번호 확인
    @PostMapping("/{accountId}/verify-password")
    public boolean verifyAuthPassword(@PathVariable Long accountId,
                                      @RequestBody String inputPassword) {
        return accountService.verifyAuthPassword(accountId, inputPassword);
    }
}
