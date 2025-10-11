package com.example.shbank.mapper;

import com.example.shbank.dto.transaction.CardPaymentResponse;
import com.example.shbank.dto.transaction.ScheduledTransferResponse;
import com.example.shbank.dto.transaction.TransactionResponse;
import com.example.shbank.entity.ScheduledTransfer;
import com.example.shbank.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    // 거래 응답 엔티티 -> DTO 변환
    @Mapping(source = "id", target = "transactionId")
    @Mapping(source = "senderAccount.user.name", target = "senderName")
    @Mapping(source = "senderAccount.accountNumber", target = "senderAccount")
    @Mapping(source = "recipientAccount.user.name", target = "recipientName")
    @Mapping(source = "recipientAccount.accountNumber", target = "recipientAccount")
    TransactionResponse toResponse(Transaction entity);

    // 예약 송금 응답 엔티티 -> DTO 변환
    @Mapping(source = "id", target = "transferId")
    @Mapping(source = "transaction.senderAccount.id", target = "accountId")
    @Mapping(source = "transaction.recipientAccount.user.name", target = "recipientName")
    @Mapping(source = "transaction.amount", target = "amount")
    @Mapping(source = "transaction.balance", target = "balance")
    @Mapping(target = "type", expression = "java(scheduledTransfer.getTransaction().getType().name())")
    ScheduledTransferResponse toScheduledTransferResponse(ScheduledTransfer scheduledTransfer);

    // 카드 결제 응답 엔티티 -> DTO 변환
    @Mapping(source = "id", target = "transactionId")
    @Mapping(source = "senderAccount.user.name", target = "senderName")
    @Mapping(source = "senderAccount.accountNumber", target = "senderAccount")
    @Mapping(source = "cardPayment.merchantName", target = "merchantName")
    CardPaymentResponse toCardPaymentResponse(Transaction entity);
}