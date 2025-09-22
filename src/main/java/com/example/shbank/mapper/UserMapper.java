package com.example.shbank.mapper;

import com.example.shbank.dto.user.MyInfoResponse;
import com.example.shbank.dto.user.MyInfoUpdateResponse;
import com.example.shbank.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    // 내 정보 응답 엔티티 -> DTO 변환
    @Mapping(source = "id", target = "userId")
    MyInfoResponse toMyInfoResponse(User user);

    // 내 정보 수정 응답 엔티티 -> DTO 변환
    MyInfoUpdateResponse toMyInfoUpdateResponse(User user);
}
