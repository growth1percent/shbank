package com.example.shbank.service;

import com.example.shbank.dto.account.AccountCreateRequest;
import com.example.shbank.dto.account.AccountCreateResponse;
import com.example.shbank.dto.account.AccountResponse;
import com.example.shbank.dto.account.AccountSettingRequest;
import com.example.shbank.entity.Account;
import com.example.shbank.entity.User;
import com.example.shbank.enums.AccountStatus;
import com.example.shbank.exception.account.AccountNotFoundException;
import com.example.shbank.mapper.AccountMapper;
import com.example.shbank.repository.AccountRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
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
    private final EntityManager entityManager;

    // 계좌 개설 (POST)
    @Transactional
    public AccountCreateResponse createAccount(AccountCreateRequest request, Long userId) {
        String encodedPassword = passwordEncoder.encode(request.getAuthPassword());
        User userProxy = entityManager.getReference(User.class, userId);

        Account account = Account.builder()
                .type(request.getAccountType())
                .balance(request.getInitialAmount())
                .status(AccountStatus.ACTIVE)
                .user(userProxy)
                .accountName(request.getAccountName())
                .accountNumber("temp")
                .transferLimit(request.getTransferLimit())
                .authPassword(encodedPassword)
                .build();

        account = accountRepository.saveAndFlush(account);
        account = account.assignAccountNumberFromId();
        account = accountRepository.save(account);

        return accountMapper.toCreateDto(account);
    }

    // 계좌 목록 조회 (GET)
    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountByUser(Long userId) {
        List<Account> accounts = accountRepository.findByUserId(userId);
        return accounts.stream().map(accountMapper::toDto).collect(Collectors.toList());
    }

    // 계좌 번호 조회 (GET)
    @Transactional(readOnly = true)
    public Account getAccountByAccountNumber(String accountNumber) throws AccountNotFoundException {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("해당 계좌가 존재하지 않습니다."));
    }

    // 계좌 설정 변경 (PATCH)
    @Transactional
    public void updateAccountSettings(Long accountId, Long userId, AccountSettingRequest request)
            throws AccountNotFoundException, AccessDeniedException {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("해당 계좌가 존재하지 않습니다."));

        if (!account.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("해당 계좌에 접근할 권한이 없습니다.");
        }

        if (request.getTransferLimit() != null) {
            account.updateTransferLimit(request.getTransferLimit());
        }

        if (request.getAuthPassword() != null) {
            String currentPassword = request.getAuthPassword().getCurrent();
            String newPassword = request.getAuthPassword().getNewPassword();

            if (!passwordEncoder.matches(currentPassword, account.getAuthPassword())) {
                throw new AccessDeniedException("현재 비밀번호가 일치하지 않습니다.");
            }

            account.updateAuthPassword(passwordEncoder.encode(newPassword));
        }

        accountRepository.save(account);
    }

    // 1회 이체 한도 조회 (GET)
    public Integer getTransferLimit(Long accountId, Long userId)
            throws AccountNotFoundException, AccessDeniedException {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("해당 계좌가 존재하지 않습니다."));

        if (!account.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("해당 계좌에 접근할 권한이 없습니다.");
        }

        return account.getTransferLimit();
    }

    // 계좌 인증 비밀번호 확인 (POST)
    public boolean verifyAuthPassword(Long accountId, String inputPassword)
            throws AccountNotFoundException, AccessDeniedException {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("해당 계좌가 존재하지 않습니다."));

        if (!passwordEncoder.matches(inputPassword, account.getAuthPassword())) {
            throw new AccessDeniedException("비밀번호가 일치하지 않습니다.");
        }

        return true;
    }
}
