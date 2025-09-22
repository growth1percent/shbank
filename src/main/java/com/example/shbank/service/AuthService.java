package com.example.shbank.service;

import com.example.shbank.dto.auth.*;
import com.example.shbank.entity.User;
import com.example.shbank.repository.UserRepository;
import com.example.shbank.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    // 회원가입
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(encodedPassword)
                .build();

        User savedUser = userRepository.save(user);

        return RegisterResponse.builder()
                .userId(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .build();
    }

    // 로그인
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        redisTemplate.opsForValue().set(
                "refreshToken:" + user.getId(),
                refreshToken,
                jwtUtil.getRefreshTokenValidity(),
                TimeUnit.MILLISECONDS
        );

        return LoginResponse.builder()
                .accessToken(accessToken)
                .accessTokenExpiresIn((int) jwtUtil.getAccessTokenValidity() / 1000)
                .refreshToken(refreshToken)
                .refreshTokenExpiresIn((int) jwtUtil.getRefreshTokenValidity() / 1000)
                .userId(user.getId())
                .userName(user.getName())
                .email(user.getEmail())
                .build();
    }

    // 액세스 토큰 재발급
    public AccessTokenResponse refreshAccessToken(AccessTokenRequest request) {
        if (!jwtUtil.validateToken(request.getRefreshToken())) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        Long userId = jwtUtil.getUserIdFromToken(request.getRefreshToken());

        String storedToken = redisTemplate.opsForValue().get("refreshToken:" + userId);
        if (storedToken == null || !storedToken.equals(request.getRefreshToken())) {
            throw new IllegalArgumentException("리프레시 토큰이 일치하지 않습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());

        return AccessTokenResponse.builder()
                .accessToken(newAccessToken)
                .accessTokenExpiresIn((int) jwtUtil.getAccessTokenValidity() / 1000)
                .refreshToken(request.getRefreshToken())
                .refreshTokenExpiresIn((int) jwtUtil.getRefreshTokenValidity() / 1000)
                .build();
    }

    // 로그아웃 (Redis에서 refreshToken 삭제)
    public void logout(Long userId) {
        redisTemplate.delete("refreshToken:" + userId);
    }
}
