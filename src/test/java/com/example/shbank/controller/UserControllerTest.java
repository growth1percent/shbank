package com.example.shbank.controller;

import com.example.shbank.dto.user.MyInfoResponse;
import com.example.shbank.dto.user.MyInfoUpdateRequest;
import com.example.shbank.dto.user.MyInfoUpdateResponse;
import com.example.shbank.exception.auth.EmailAlreadyExistsException;
import com.example.shbank.exception.auth.UnauthorizedException;
import com.example.shbank.security.CustomUserDetails;
import com.example.shbank.service.UserService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JWTUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomUserDetails userDetails;

    @BeforeEach
    void setupAuthentication() {
        userDetails = new CustomUserDetails(
                new com.example.shbank.entity.User(1L, "test@example.com", "홍길동", "password")
        );

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testGetMyInfo_Success() throws Exception {
        MyInfoResponse response = new MyInfoResponse(1L, "홍길동", "test@example.com");
        Mockito.when(userService.getMyInfo(userDetails.getUserId())).thenReturn(response);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.name").value("홍길동"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void testGetMyInfo_Unauthorized() throws Exception {
        Mockito.when(userService.getMyInfo(userDetails.getUserId()))
                .thenThrow(new UnauthorizedException("사용자를 찾을 수 없습니다."));

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateMyInfo_Success() throws Exception {
        MyInfoUpdateRequest request = MyInfoUpdateRequest.builder()
                .email("new@example.com")
                .build();

        MyInfoUpdateResponse response = new MyInfoUpdateResponse("홍길동", "new@example.com");
        Mockito.when(userService.updateMyInfo(eq(userDetails.getUserId()), any(MyInfoUpdateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/users/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("홍길동"))
                .andExpect(jsonPath("$.email").value("new@example.com"));
    }

    @Test
    void testUpdateMyInfo_EmailAlreadyExists() throws Exception {
        MyInfoUpdateRequest request = MyInfoUpdateRequest.builder()
                .email("existing@example.com")
                .build();

        Mockito.when(userService.updateMyInfo(eq(userDetails.getUserId()), any(MyInfoUpdateRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("이미 사용 중인 이메일입니다."));

        mockMvc.perform(patch("/api/users/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateMyInfo_IncorrectCurrentPassword() throws Exception {
        MyInfoUpdateRequest.Password password = new MyInfoUpdateRequest.Password("wrong", "newpass");
        MyInfoUpdateRequest request = MyInfoUpdateRequest.builder()
                .email("new@example.com")
                .password(password)
                .build();

        Mockito.when(userService.updateMyInfo(eq(userDetails.getUserId()), any(MyInfoUpdateRequest.class)))
                .thenThrow(new UnauthorizedException("현재 비밀번호가 일치하지 않습니다."));

        mockMvc.perform(patch("/api/users/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}