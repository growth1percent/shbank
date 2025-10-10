package com.example.shbank.controller;

import com.example.shbank.dto.user.MyInfoUpdateRequest;
import com.example.shbank.dto.user.MyInfoResponse;
import com.example.shbank.dto.user.MyInfoUpdateResponse;
import com.example.shbank.security.CustomUserDetails;
import com.example.shbank.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<MyInfoResponse> getMyInfo(Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();
        MyInfoResponse response = userService.getMyInfo(userId);
        return ResponseEntity.ok(response);
    }

    // 내 정보 수정
    @PatchMapping("/me")
    public ResponseEntity<MyInfoUpdateResponse> updateMyInfo(
            Authentication authentication,
            @Valid @RequestBody MyInfoUpdateRequest request
    ) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();
        MyInfoUpdateResponse response = userService.updateMyInfo(userId, request);
        return ResponseEntity.ok(response);
    }
}
