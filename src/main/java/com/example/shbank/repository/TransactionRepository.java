package com.example.shbank.repository;

import com.example.shbank.entity.Transaction;
import com.example.shbank.enums.TransactionStatus;
import com.example.shbank.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.LocalDateTime;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // 거래 내역 조회
    List<Transaction> findByAccountId(Long accountId);

    // 거래 날짜 조건 검색
    List<Transaction> findByAccountIdAndTransactionDateBetween(Long accountId, LocalDateTime start, LocalDateTime end);

    // 거래 유형에 따른 검색
    List<Transaction> findByAccountIdAndType(Long accountId, TransactionType type);

    // 거래 날짜 + 유형 검색
    List<Transaction> findByAccountIdAndTypeAndTransactionDateBetween(Long id, TransactionType type, LocalDateTime start, LocalDateTime end);

    // 예약 송금 조회
    List<Transaction> findBySenderAccount_IdAndStatus(Long senderAccountId, TransactionStatus status);
}
