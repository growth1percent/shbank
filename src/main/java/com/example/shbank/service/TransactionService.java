package com.example.shbank.service;

import com.example.shbank.dto.transaction.CardPaymentResponse;
import com.example.shbank.dto.transaction.ScheduledTransferResponse;
import com.example.shbank.dto.transaction.TransactionHistoryResponse;
import com.example.shbank.dto.transaction.TransactionResponse;
import com.example.shbank.entity.Account;
import com.example.shbank.entity.CardPayment;
import com.example.shbank.entity.ScheduledTransfer;
import com.example.shbank.entity.Transaction;
import com.example.shbank.enums.TransactionStatus;
import com.example.shbank.enums.TransactionType;
import com.example.shbank.exception.account.AccountNotFoundException;
import com.example.shbank.exception.transaction.InsufficientBalanceException;
import com.example.shbank.mapper.TransactionMapper;
import com.example.shbank.repository.AccountRepository;
import com.example.shbank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
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
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("해당 계좌가 존재하지 않습니다."));

        if (!account.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("해당 계좌에 접근할 권한이 없습니다.");
        }
        List<Transaction> transactions;

        boolean hasDate = start != null && end != null;

        if (hasDate) {
            transactions = transactionRepository
                    .findByAccountIdAndStatusAndTransactionDateBetween(
                            account.getId(), TransactionStatus.COMPLETED, start, end);
        } else {
            transactions = transactionRepository
                    .findByAccountIdAndStatus(
                            account.getId(), TransactionStatus.COMPLETED);
        }

        int totalIn = transactions.stream()
                .filter(t -> t.getRecipientAccount() != null &&
                        t.getRecipientAccount().getId().equals(account.getId()))
                .mapToInt(Transaction::getAmount)
                .sum();

        int totalOut = transactions.stream()
                .filter(t -> t.getSenderAccount().getId().equals(account.getId()))
                .mapToInt(Transaction::getAmount)
                .sum();

        int netChange = totalIn - totalOut;

        List<TransactionResponse> transactionResponses = transactions.stream()
                .map(transactionMapper::toResponse)
                .toList();

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
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("해당 계좌가 존재하지 않습니다."));

        if (!account.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("해당 계좌에 접근할 권한이 없습니다.");
        }
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
    public Object transfer(Long senderAccountId,
                           String recipientAccountNumber,
                           Integer amount,
                           LocalDateTime scheduleDate, // 예약 송금일, null이면 즉시 송금
                           String memo,
                           TransactionType type,
                           Long userId) {

        // 계좌 조회 및 검증
        Account senderAccount = accountRepository.findById(senderAccountId)
                .orElseThrow(() -> new AccountNotFoundException("송금 계좌가 존재하지 않습니다."));

        if (!senderAccount.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("해당 계좌에 접근할 권한이 없습니다.");
        }

        Account recipientAccount = accountRepository.findByAccountNumber(recipientAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException("수취 계좌가 존재하지 않습니다."));

        // 잔액 체크
        if (senderAccount.getBalance() < amount) {
            throw new InsufficientBalanceException("잔액이 부족합니다.");
        }

        boolean isScheduled = scheduleDate != null;

        senderAccount.withdraw(amount);

        ScheduledTransfer scheduledTransfer = null;
        if (isScheduled) {
            scheduledTransfer = ScheduledTransfer.builder()
                    .scheduleDate(scheduleDate)
                    .memo(memo)
                    .build();
        }

        // Transaction 생성
        Transaction transaction = Transaction.builder()
                .senderAccount(senderAccount)
                .recipientAccount(recipientAccount)
                .amount(amount)
                .balance(senderAccount.getBalance())
                .type(type)
                .status(isScheduled ? TransactionStatus.SCHEDULED : TransactionStatus.COMPLETED)
                .build();

        if (isScheduled) {
            transaction.linkScheduledTransfer(scheduledTransfer);
            scheduledTransfer.linkTransaction(transaction);
        }

        transactionRepository.save(transaction);

        if (isScheduled) {
            return transactionMapper.toScheduledTransferResponse(transaction.getScheduledTransfer());
        } else {
            return transactionMapper.toResponse(transaction);
        }
    }

    // 예약 송금 취소 (PATCH)
    @Transactional
    public void cancelScheduledTransfer(Long transactionId, Long userId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new AccountNotFoundException("해당 거래가 존재하지 않습니다."));

        Account senderAccount = transaction.getSenderAccount();
        if (!senderAccount.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("취소 권한이 없습니다.");
        }

        if (transaction.getStatus() != TransactionStatus.SCHEDULED) {
            throw new IllegalStateException("예약 송금만 취소할 수 있습니다.");
        }

        senderAccount.deposit(transaction.getAmount());
        transaction.cancelScheduled();
    }

    // 카드 결제 (POST)
    @Transactional
    public CardPaymentResponse cardPayment(Long accountId,
                                           Integer amount,
                                           String merchantName) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("결제 계좌가 존재하지 않습니다."));

        if (account.getBalance() < amount) {
            throw new InsufficientBalanceException("잔액이 부족합니다.");
        }

        account.withdraw(amount);

        CardPayment cardPayment = CardPayment.builder()
                .merchantName(merchantName)
                .build();

        Transaction transaction = Transaction.builder()
                .senderAccount(account)
                .amount(amount)
                .balance(account.getBalance())
                .type(TransactionType.TRANSFER_OUT)
                .status(TransactionStatus.COMPLETED)
                .cardPayment(cardPayment)
                .build();

        transaction.linkCardPayment(cardPayment);

        transactionRepository.save(transaction);

        return transactionMapper.toCardPaymentResponse(transaction);
    }
}
