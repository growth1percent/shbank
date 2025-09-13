package com.example.shbank.mapper;

import com.example.shbank.dto.user.UserLoginRequest;
import com.example.shbank.dto.user.UserSignUpRequest;
import com.example.shbank.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    // 로그인 요청 DTO -> 엔티티
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    User toLoginEntity(UserLoginRequest dto);

    // 회원가입 요청 DTO -> 엔티티
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    User toSignUpEntity(UserSignUpRequest dto);
}
