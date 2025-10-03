package com.example.shbank.mapper;

import com.example.shbank.dto.transaction.ScheduledTransferResponse;
import com.example.shbank.dto.transaction.TransactionResponse;
import com.example.shbank.dto.transaction.TransactionRequest;
import com.example.shbank.entity.ScheduledTransfer;
import com.example.shbank.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    // 거래 응답 엔티티 -> DTO 변환
    @Mapping(source = "id", target = "transactionId")
    @Mapping(source = "senderAccount.accountName", target = "senderName")
    @Mapping(source = "senderAccount.accountNumber", target = "senderAccount")
    @Mapping(source = "recipientAccount.accountName", target = "recipientName")
    @Mapping(source = "recipientAccount.accountNumber", target = "recipientAccount")
    TransactionResponse toResponse(Transaction entity);

    // 예약 송금 응답 엔티티 -> DTO 변환
    @Mapping(source = "transaction.id", target = "transferId")
    @Mapping(source = "transaction.senderAccount.id", target = "accountId")
    @Mapping(source = "transaction.recipientAccount.accountName", target = "recipientName")
    @Mapping(source = "transaction.amount", target = "amount")
    @Mapping(source = "scheduleDate", target = "scheduleDate")
    @Mapping(source = "memo", target = "memo")
    ScheduledTransferResponse toScheduledTransferResponse(ScheduledTransfer scheduledTransfer);
}