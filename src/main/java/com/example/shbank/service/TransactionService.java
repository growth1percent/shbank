package com.example.shbank.service;

import com.example.shbank.dto.transaction.ScheduledTransferResponse;
import com.example.shbank.dto.transaction.TransactionHistoryResponse;
import com.example.shbank.dto.transaction.TransactionResponse;
import com.example.shbank.entity.Account;
import com.example.shbank.entity.CardPayment;
import com.example.shbank.entity.ScheduledTransfer;
import com.example.shbank.entity.Transaction;
import com.example.shbank.enums.TransactionStatus;
import com.example.shbank.enums.TransactionType;
import com.example.shbank.mapper.TransactionMapper;
import com.example.shbank.repository.AccountRepository;
import com.example.shbank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionMapper transactionMapper;

    // 거래 내역 조회 (GET)
    @Transactional(readOnly = true)
    public TransactionHistoryResponse getTransactionHistory(Long accountId,
                                                     Long userId,
                                                     TransactionType type,
                                                     LocalDateTime start,
                                                     LocalDateTime end) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 계좌가 존재하지 않거나 권한이 없습니다."));

        List<Transaction> transactions;

        boolean hasDate = start != null && end != null;

        if (type == null) { // 전체 거래
            if (hasDate) {
                transactions = transactionRepository.findByAccountIdAndTransactionDateBetween(
                        account.getId(), start, end);
            } else {
                transactions = transactionRepository.findByAccountId(account.getId());
            }
        } else { // 특정 거래 유형 (TRANSFER_IN, TRANSFER_OUT)
            if (hasDate) {
                transactions = transactionRepository.findByAccountIdAndTypeAndTransactionDateBetween(
                        account.getId(), type, start, end);
            } else {
                transactions = transactionRepository.findByAccountIdAndType(account.getId(), type);
            }
        }

        List<TransactionResponse> transactionResponses = transactions.stream()
                .map(transactionMapper::toResponse)
                .toList();

        int totalIn = transactions.stream()
                .filter(t -> t.getType() == TransactionType.TRANSFER_IN)
                .mapToInt(Transaction::getAmount)
                .sum();

        int totalOut = transactions.stream()
                .filter(t -> t.getType() == TransactionType.TRANSFER_OUT)
                .mapToInt(Transaction::getAmount)
                .sum();

        int netChange = totalIn - totalOut;

        return TransactionHistoryResponse.builder()
                .totalIn(totalIn)
                .totalOut(totalOut)
                .netChange(netChange)
                .transactions(transactionResponses)
                .build();
    }

    // 예약 송금 목록 조회 (GET)
    @Transactional(readOnly = true)
    public List<ScheduledTransferResponse> getScheduledTransfers(Long accountId, Long userId) {
        // 계좌 검증
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 계좌가 존재하지 않거나 권한이 없습니다."));

        // status = SCHEDULED 인 Transaction 조회
        List<Transaction> scheduledTransactions =
                transactionRepository.findBySenderAccount_IdAndStatus(account.getId(), TransactionStatus.SCHEDULED);

        // ScheduledTransferResponse DTO로 변환
        return scheduledTransactions.stream()
                .filter(tx -> tx.getScheduledTransfer() != null)
                .map(tx -> transactionMapper.toScheduledTransferResponse(tx.getScheduledTransfer()))
                .toList();
    }

    // 이체 (즉시/예약 송금) (POST)
    @Transactional
    public TransactionResponse transfer(Long senderAccountId,
                                        String recipientAccountNumber,
                                        Integer amount,
                                        LocalDateTime scheduleDate, // 예약 송금일, null이면 즉시 송금
                                        String memo,
                                        TransactionType type,
                                        Long userId) {

        // 계좌 조회 및 검증
        Account sender = accountRepository.findByIdAndUserId(senderAccountId, userId)
                .orElseThrow(() -> new IllegalArgumentException("송금 계좌가 존재하지 않거나 권한이 없습니다."));

        Account recipient = accountRepository.findByAccountNumber(recipientAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException("수취 계좌가 존재하지 않습니다."));

        // 잔액 체크
        if (sender.getBalance() < amount) {
            throw new IllegalArgumentException("잔액 부족");
        }

        // ScheduledTransfer 생성 (예약 송금일이 있는 경우)
        ScheduledTransfer scheduledTransfer = null;
        if (scheduleDate != null) {
            scheduledTransfer = ScheduledTransfer.builder()
                    .scheduleDate(scheduleDate)
                    .memo(memo)
                    .build();
        }

        // Transaction 생성
        Transaction transaction = Transaction.builder()
                .senderAccount(sender)
                .recipientAccount(recipient)
                .amount(amount)
                .balance(sender.getBalance() - amount)
                .type(type)
                .status(scheduleDate != null ? TransactionStatus.SCHEDULED : TransactionStatus.COMPLETED)
                .scheduledTransfer(scheduledTransfer)
                .build();

        if (scheduledTransfer != null) {
            scheduledTransfer = ScheduledTransfer.builder()
                    .transaction(transaction)
                    .scheduleDate(scheduledTransfer.getScheduleDate())
                    .memo(scheduledTransfer.getMemo())
                    .build();

            transaction = Transaction.builder()
                    .senderAccount(transaction.getSenderAccount())
                    .recipientAccount(transaction.getRecipientAccount())
                    .amount(transaction.getAmount())
                    .balance(transaction.getBalance())
                    .type(transaction.getType())
                    .status(transaction.getStatus())
                    .scheduledTransfer(scheduledTransfer)
                    .build();
        }

        transactionRepository.save(transaction);

        return transactionMapper.toResponse(transaction);
    }
    // 예약 송금 취소 (PATCH)
    @Transactional
    public void cancelScheduledTransfer(Long transactionId, Long userId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 거래가 존재하지 않습니다."));

        Account senderAccount = transaction.getSenderAccount();
        if (!senderAccount.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("취소 권한이 없습니다.");
        }

        if (transaction.getStatus() != TransactionStatus.SCHEDULED) {
            throw new IllegalStateException("예약 송금만 취소할 수 있습니다.");
        }

        senderAccount.deposit(transaction.getAmount());
        transaction.cancelScheduled();
    }
    // 카드 결제 (POST)
    @Transactional
    public TransactionResponse cardPayment(Long accountId,
                                           Integer amount,
                                           String merchantName) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("결제 계좌가 존재하지 않습니다."));

        if (account.getBalance() < amount) {
            throw new IllegalArgumentException("잔액 부족");
        }

        Transaction transaction = Transaction.builder()
                .senderAccount(account)
                .recipientAccount(null)
                .amount(amount)
                .balance(account.getBalance() - amount)
                .type(TransactionType.TRANSFER_OUT)
                .status(TransactionStatus.COMPLETED)
                .build();

        CardPayment cardPayment = CardPayment.builder()
                .transaction(transaction)
                .merchantName(merchantName)
                .build();

        transaction.linkCardPayment(cardPayment);

        account.withdraw(amount);

        transactionRepository.save(transaction);

        return transactionMapper.toResponse(transaction);
    }
}
