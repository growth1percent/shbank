package com.example.shbank.service;

import com.example.shbank.dto.account.AccountCreateRequest;
import com.example.shbank.dto.account.AccountResponse;
import com.example.shbank.dto.account.AccountSettingRequest;
import com.example.shbank.entity.Account;
import com.example.shbank.entity.User;
import com.example.shbank.mapper.AccountMapper;
import com.example.shbank.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;

    // 계좌 개설 (POST)
    @Transactional
    public AccountResponse createAccount(AccountCreateRequest request, User loggedInUser) {
        // authPassword 암호화
        String encodedPassword = passwordEncoder.encode(request.getAuthPassword());

        Account account = Account.createAccount(request, loggedInUser, encodedPassword);
        account = accountRepository.saveAndFlush(account);
        account = account.assignAccountNumberFromId();
        account = accountRepository.save(account);

        return accountMapper.toDto(account);
    }

    private String generateAccountNumber() {
        int middle = (int) (Math.random() * 900) + 100;
        int last = (int) (Math.random() * 900000) + 100000;
        return String.format("1234-%03d-%06d", middle, last);
    }

    // 계좌 목록 조회 (GET)
    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountByUser(Long userId) {
        List<Account> accounts = accountRepository.findByUserId(userId);

        return accounts.stream().map(accountMapper::toDto).collect(Collectors.toList());
    }

    // 계좌 번호 조회 (GET)
    @Transactional(readOnly = true)
    public Account getAccountByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("해당 계좌가 존재하지 않습니다."));
    }

    // 계좌 설정 변경 (PATCH)
    @Transactional
    public void updateAccountSettings(Long accountId, Long userId, AccountSettingRequest request) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 계좌가 존재하지 않거나 권한이 없습니다."));

        // transferLimit 변경
        if (request.getTransferLimit() != null) {
            account.updateTransferLimit(request.getTransferLimit());
        }

        // authPassword 변경
        if (request.getAuthPassword() != null) {
            String currentPassword = request.getAuthPassword().getCurrent();
            String newPassword = request.getAuthPassword().getNewPassword();

            // 현재 비밀번호 확인
            if (!passwordEncoder.matches(currentPassword, account.getAuthPassword())) {
                throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
            }

            // 새 비밀번호 암호화 후 업데이트
            account.updateAuthPassword(passwordEncoder.encode(newPassword));
        }

        accountRepository.save(account);
    }

    // 1회 이체 한도 조회 (GET)
    public Integer getTransferLimit(Long accountId, Long userId) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 계좌가 존재하지 않거나 권한이 없습니다."));
        return account.getTransferLimit();
    }

    // 계좌 인증 비밀번호 확인 (POST)
    public boolean verifyAuthPassword(Long accountId, String inputPassword) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("해당 계좌가 존재하지 않습니다."));
        return passwordEncoder.matches(inputPassword, account.getAuthPassword());
    }
}
