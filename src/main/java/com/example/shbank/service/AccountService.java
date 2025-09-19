package com.example.shbank.service;

import com.example.shbank.dto.account.AccountCreateRequest;
import com.example.shbank.dto.account.AccountResponse;
import com.example.shbank.entity.User;
import com.example.shbank.mapper.AccountMapper;
import com.example.shbank.repository.AccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Transactional
    public AccountResponse createAccount(AccountCreateRequest request, User loggedInUser) {

    }
}
