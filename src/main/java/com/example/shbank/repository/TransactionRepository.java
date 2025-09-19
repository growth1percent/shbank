package com.example.shbank.repository;

import com.example.shbank.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.LocalDateTime;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // 거래 목록 조회
    List<Transaction> findByAccountId(Long accountId);

    // 거래 목록 조건 검색
    List<Transaction> findByAccountIdAndTransactionDateBetween(Long accountId, LocalDateTime start, LocalDateTime end);
}
