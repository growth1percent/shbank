package com.example.shbank.mapper;

import com.example.shbank.dto.transaction.TransactionHistoryResponse;
import com.example.shbank.dto.transaction.TransactionResponse;
import com.example.shbank.dto.transaction.TransactionRequest;
import com.example.shbank.entity.Account;
import com.example.shbank.entity.Transaction;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    // 거래 요청 DTO -> 엔티티 변환
    @Mapping(target = "scheduledTransfer", ignore = true)
    @Mapping(target = "cardPayment", ignore = true)
    Transaction toEntity(TransactionRequest dto);

    // 거래 응답 엔티티 -> DTO 변환
    @Mapping(source = "id", target = "transactionId")
    @Mapping(source = "senderAccount.accountName", target = "senderName")
    @Mapping(source = "senderAccount.accountNumber", target = "senderAccount")
    @Mapping(source = "recipientAccount.accountName", target = "recipientName")
    @Mapping(source = "recipientAccount.accountNumber", target = "recipientAccount")
    TransactionResponse toResponse(Transaction entity);

    // 거래 내역 응답 엔티티 -> DTO 변환
    List<TransactionResponse> toResponseList(List<Transaction> transactions);
}