package com.example.shbank.mapper;

import com.example.shbank.dto.account.AccountCreateRequest;
import com.example.shbank.dto.account.AccountCreateResponse;
import com.example.shbank.dto.account.AccountResponse;
import com.example.shbank.dto.account.AccountSettingRequest;
import com.example.shbank.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    // 계좌 개설 DTO → 엔티티 변환
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountNumber", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "authPassword", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(source = "accountType", target = "type")
    @Mapping(source = "initialAmount", target = "balance")
    Account toCreateAccount(AccountCreateRequest dto);

    // 계좌 개설 엔티티 -> DTO 변환
    @Mapping(source = "id", target = "accountId")
    @Mapping(source = "type", target = "accountType")
    AccountCreateResponse toCreateResponse(Account account);

    // 계좌 조회 엔티티 -> DTO 변환
    @Mapping(source = "id", target = "accountId")
    @Mapping(source = "type", target = "accountType")
    AccountResponse toResponse(Account account);

    // 계좌 비밀번호 변경 DTO -> 엔티티 변환
    @Mapping(target = "authPassword", ignore = true)
    Account toSettingRequest(AccountSettingRequest dto, @MappingTarget Account entity);
}
