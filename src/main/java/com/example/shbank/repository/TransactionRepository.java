package com.example.shbank.repository;

import com.example.shbank.entity.Transaction;
import com.example.shbank.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.LocalDateTime;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // 예약 송금 조회
    List<Transaction> findBySenderAccount_IdAndStatus(Long senderAccountId, TransactionStatus status);

    // 날짜 범위 있는 경우
    @Query("""
       SELECT t
       FROM Transaction t
       WHERE (t.senderAccount.id = :accountId OR t.recipientAccount.id = :accountId)
         AND t.status = :status
         AND t.transactionDate BETWEEN :start AND :end
       """)
    List<Transaction> findByAccountIdAndStatusAndTransactionDateBetween(
            @Param("accountId") Long accountId,
            @Param("status") TransactionStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // 날짜 범위 없는 경우
    @Query("""
       SELECT t
       FROM Transaction t
       WHERE (t.senderAccount.id = :accountId OR t.recipientAccount.id = :accountId)
         AND t.status = :status
       """)
    List<Transaction> findByAccountIdAndStatus(
            @Param("accountId") Long accountId,
            @Param("status") TransactionStatus status
    );
}
