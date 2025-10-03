package com.example.shbank.repository;

import com.example.shbank.entity.Transaction;
import com.example.shbank.enums.TransactionStatus;
import com.example.shbank.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.LocalDateTime;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // 전체 거래 내역
    List<Transaction> findBySenderAccount_IdOrRecipientAccount_Id(Long senderId, Long recipientId);

    // 날짜 조건 검색
    List<Transaction> findBySenderAccount_IdOrRecipientAccount_IdAndTransactionDateBetween(
            Long senderId, Long recipientId, LocalDateTime start, LocalDateTime end);

    // 거래 유형에 따른 검색
    List<Transaction> findBySenderAccount_IdOrRecipientAccount_IdAndType(
            Long senderId, Long recipientId, TransactionType type);

    // 거래 유형에 따른 날짜 조건 검색
    List<Transaction> findBySenderAccount_IdOrRecipientAccount_IdAndTypeAndTransactionDateBetween(
            Long senderId, Long recipientId, TransactionType type, LocalDateTime start, LocalDateTime end);

    // 예약 송금 조회
    List<Transaction> findBySenderAccount_IdAndStatus(Long senderAccountId, TransactionStatus status);
}
