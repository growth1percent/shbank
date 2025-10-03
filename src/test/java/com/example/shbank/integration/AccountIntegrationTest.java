package com.example.shbank.integration;

import com.example.shbank.dto.account.*;
import com.example.shbank.dto.verify.VerifyPasswordRequest;
import com.example.shbank.entity.Account;
import com.example.shbank.entity.User;
import com.example.shbank.enums.AccountType;
import com.example.shbank.repository.AccountRepository;
import com.example.shbank.repository.UserRepository;
import com.example.shbank.util.JWTUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AccountIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTUtil jwtUtil;

    private String jwtToken;

    @Container
    public static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("shbank_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void setDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
    }

    private String baseUrl;
    private User savedUser;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/accounts";
        accountRepository.deleteAll();

        User testUser = User.builder()
                .email("test@example.com")
                .name("홍길동")
                .password("password")
                .build();

        savedUser = userRepository.save(testUser);

        jwtToken = jwtUtil.generateAccessToken(savedUser.getId(), savedUser.getEmail());
    }

    @Test
    void testAccount() {
        AccountCreateRequest request = AccountCreateRequest.builder()
                .accountName("Test Account")
                .accountType(AccountType.SAVINGS)
                .initialAmount(10000)
                .transferLimit(5000)
                .authPassword("123456")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwtToken);

        HttpEntity<AccountCreateRequest> entity = new HttpEntity<>(request, headers);

        // 계좌 생성 요청
        ResponseEntity<AccountCreateResponse> response = restTemplate.postForEntity(baseUrl, entity, AccountCreateResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();

        // 단일 계좌 조회
        HttpEntity<Void> getEntity = new HttpEntity<>(headers);

        ResponseEntity<AccountResponse> getResponse = restTemplate.exchange(
                baseUrl + "/" + response.getBody().getAccountNumber(),
                HttpMethod.GET,
                getEntity,
                AccountResponse.class
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getAccountName()).isEqualTo("Test Account");

        // 계좌 목록 조회
        ResponseEntity<AccountResponse[]> getResponseList = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                getEntity,
                AccountResponse[].class
        );

        AccountResponse[] responseBody = getResponseList.getBody();

        assertThat(getResponseList.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseBody).isNotNull();
        assertThat(responseBody[0].getAccountName()).isEqualTo("Test Account");

        // 계좌 비밀번호 인증
        VerifyPasswordRequest verifyRequest = VerifyPasswordRequest.builder()
                .authPassword("123456")
                .build();

        HttpEntity<VerifyPasswordRequest> verifyEntity = new HttpEntity<>(verifyRequest, headers);

        ResponseEntity<Boolean> verifyResponse = restTemplate.postForEntity(baseUrl + "/" + 1L + "/verify-password", verifyEntity, Boolean.class);

        assertThat(verifyResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(verifyResponse.getBody()).isTrue();

        // 계좌 설정 업데이트 (1회 이체 한도)
        AccountSettingRequest accountSettingRequest = AccountSettingRequest.builder()
                .transferLimit(50000)
                .build();

        HttpEntity<AccountSettingRequest> settingEntity = new HttpEntity<>(accountSettingRequest, headers);

        ResponseEntity<Void> settingResponse = restTemplate.exchange(
                baseUrl + "/" + 1L + "/settings",
                HttpMethod.PATCH,
                settingEntity,
                Void.class
        );
        Account updatedAccount = accountRepository.findById(1L).orElseThrow();
        assertThat(settingResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updatedAccount.getTransferLimit()).isEqualTo(50000);

        // 계좌 설정 업데이트 (비밀번호 변경)
        AccountSettingRequest passwordRequest = AccountSettingRequest.builder()
                .authPassword(
                        AccountSettingRequest.AuthPassword.builder()
                                .current("123456")
                                .newPassword("654321")
                                .build()
                )
                .build();

        HttpEntity<AccountSettingRequest> passwordEntity = new HttpEntity<>(passwordRequest, headers);

        ResponseEntity<Void> passwordResponse = restTemplate.exchange(
                baseUrl + "/" + 1L + "/settings",
                HttpMethod.PATCH,
                passwordEntity,
                Void.class
        );

        Account passwordChangedAccount = accountRepository.findById(1L).orElseThrow();
        assertThat(passwordResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(passwordEncoder.matches("654321", passwordChangedAccount.getAuthPassword())).isTrue();

        // 1회 이체 한도 조회
        ResponseEntity<Integer> getLimitResponse = restTemplate.exchange(
                baseUrl + "/" + 1L + "/settings",
                HttpMethod.GET,
                getEntity,
                Integer.class
        );

        assertThat(getLimitResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getLimitResponse.getBody()).isEqualTo(50000);
    }
}
