package com.example.shbank.mapper;

import com.example.shbank.dto.account.AccountCreateRequest;
import com.example.shbank.dto.account.AccountResponse;
import com.example.shbank.dto.account.AccountSettingResponse;
import com.example.shbank.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    // 계좌 개설 DTO → 엔티티 변환
    @Mapping(target = "id", ignore = true) // PK는 DB가 생성
    @Mapping(target = "accountNumber", ignore = true) // 계좌번호는 서비스에서 생성
    @Mapping(target = "user", ignore = true) // 로그인된 유저로 서비스에서 세팅
    @Mapping(target = "authPassword", ignore = true) // 비밀번호는 서비스에서 세팅
    @Mapping(target = "status", constant = "ACTIVE") // 기본값
    @Mapping(source = "accountType", target = "type") // DTO accountType → 엔티티 type
    @Mapping(source = "initialAmount", target = "balance") // 초기금액 → balance
    Account toEntity(AccountCreateRequest dto);

    // 계좌 조회 엔티티 -> DTO 변환
    @Mapping(source = "id", target = "accountId")
    @Mapping(source = "type", target = "accountType")
    AccountResponse toDto(Account account);

    // 1회 이체한도 조회 엔티티 -> DTO 변환
    @Mapping(source = "id", target = "accountId")
    AccountSettingResponse toSettingDto(Account account);




}
