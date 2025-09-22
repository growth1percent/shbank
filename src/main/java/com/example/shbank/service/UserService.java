package com.example.shbank.service;

import com.example.shbank.dto.user.MyInfoUpdateRequest;
import com.example.shbank.dto.user.MyInfoResponse;
import com.example.shbank.dto.user.MyInfoUpdateResponse;
import com.example.shbank.entity.User;
import com.example.shbank.mapper.UserMapper;
import com.example.shbank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // 내 정보 조회 (GET)
    public MyInfoResponse getMyInfo(Long userId) {
        return userRepository.findById(userId)
                .map(userMapper::toMyInfoResponse)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    // 내 정보 수정 (PATCH)
    @Transactional
    public MyInfoUpdateResponse updateMyInfo(Long userId, MyInfoUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 이메일 변경
        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
            }
            user.updateEmail(request.getEmail());
        }

        // 비밀번호 변경
        if (request.getPassword() != null && request.getPassword().getCurrent() != null
                && request.getPassword().getNewPassword() != null) {
            if (!passwordEncoder.matches(request.getPassword().getCurrent(), user.getPassword())) {
                throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
            }
            user.updatePassword(passwordEncoder.encode(request.getPassword().getNewPassword()));
        }

        return userMapper.toMyInfoUpdateResponse(user);
    }
}
