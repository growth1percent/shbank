package com.example.shbank.controller;

import com.example.shbank.dto.auth.*;
import com.example.shbank.exception.auth.EmailAlreadyExistsException;
import com.example.shbank.exception.auth.UnauthorizedException;
import com.example.shbank.security.CustomUserDetails;
import com.example.shbank.service.AuthService;
import com.example.shbank.util.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JWTUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomUserDetails userDetails;

    @BeforeEach
    void setupAuthentication() {
        userDetails = new CustomUserDetails(
                new com.example.shbank.entity.User(1L, "test@example.com", "홍길동", "123456")
        );
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testRegisterSuccess() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("홍길동")
                .email("test@example.com")
                .password("123456")
                .build();

        RegisterResponse response = RegisterResponse.builder()
                .userId(1L)
                .name("홍길동")
                .email("test@example.com")
                .build();

        Mockito.when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(response.getUserId()))
                .andExpect(jsonPath("$.name").value(response.getName()))
                .andExpect(jsonPath("$.email").value(response.getEmail()));
    }

    @Test
    void testRegister_EmailAlreadyExists() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("홍길동")
                .email("test@example.com")
                .password("123456")
                .build();

        Mockito.when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("이미 가입된 이메일입니다."));

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("이미 가입된 이메일입니다."));
    }

    @Test
    void testLoginSuccess() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("123456")
                .build();

        LoginResponse response = LoginResponse.builder()
                .accessToken("access-token")
                .accessTokenExpiresIn(3600)
                .refreshToken("refresh-token")
                .refreshTokenExpiresIn(604800)
                .userId(1L)
                .userName("홍길동")
                .email("test@example.com")
                .build();

        Mockito.when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(response.getAccessToken()))
                .andExpect(jsonPath("$.refreshToken").value(response.getRefreshToken()))
                .andExpect(jsonPath("$.userId").value(response.getUserId()))
                .andExpect(jsonPath("$.userName").value(response.getUserName()))
                .andExpect(jsonPath("$.email").value(response.getEmail()))
                .andExpect(jsonPath("$.accessTokenExpiresIn").value(response.getAccessTokenExpiresIn()))
                .andExpect(jsonPath("$.refreshTokenExpiresIn").value(response.getRefreshTokenExpiresIn()));
    }

    @Test
    void testLogin_UserNotFound() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("notfound@example.com")
                .password("123456")
                .build();

        Mockito.when(authService.login(any(LoginRequest.class)))
                .thenThrow(new UnauthorizedException("사용자를 찾을 수 없습니다."));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("사용자를 찾을 수 없습니다."));
    }

    @Test
    void testRefreshAccessTokenSuccess() throws Exception {
        AccessTokenRequest request = AccessTokenRequest.builder()
                .refreshToken("refresh-token")
                .build();

        AccessTokenResponse response = AccessTokenResponse.builder()
                .accessToken("new-access-token")
                .accessTokenExpiresIn(3600)
                .refreshToken("refresh-token")
                .refreshTokenExpiresIn(604800)
                .build();

        Mockito.when(authService.refreshAccessToken(any(AccessTokenRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/refresh")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(response.getAccessToken()))
                .andExpect(jsonPath("$.refreshToken").value(response.getRefreshToken()))
                .andExpect(jsonPath("$.accessTokenExpiresIn").value(response.getAccessTokenExpiresIn()))
                .andExpect(jsonPath("$.refreshTokenExpiresIn").value(response.getRefreshTokenExpiresIn()));
    }

    @Test
    void testRefreshAccessToken_InvalidToken() throws Exception {
        AccessTokenRequest request = AccessTokenRequest.builder()
                .refreshToken("invalid-token")
                .build();

        Mockito.when(authService.refreshAccessToken(any(AccessTokenRequest.class)))
                .thenThrow(new UnauthorizedException("유효하지 않은 리프레시 토큰입니다."));

        mockMvc.perform(post("/api/auth/refresh")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("유효하지 않은 리프레시 토큰입니다."));
    }

    @Test
    void testLogoutSuccess() throws Exception {
        Mockito.doNothing().when(authService).logout(eq(userDetails.getUserId()));

        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
