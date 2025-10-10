package com.example.shbank.repository;

import com.example.shbank.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    // 계좌 조회
    List<Account> findByUserId(Long userId);

    // 계좌 조회 + 소유자 체크
    Optional<Account> findByIdAndUserId(Long accountId, Long userId);

    // 계좌 번호 조회
    Optional<Account> findByAccountNumber(String accountNumber);
}
