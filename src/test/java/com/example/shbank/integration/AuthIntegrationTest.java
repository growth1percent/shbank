package com.example.shbank.integration;

import com.example.shbank.dto.auth.*;
import com.example.shbank.repository.UserRepository;
import com.example.shbank.util.JWTUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AuthIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JWTUtil jwtUtil;

    @Container
    public static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("shbank_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    public static GenericContainer<?> redisContainer = new GenericContainer<>("redis:8.2.2")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void setDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.redis.host", redisContainer::getHost);
        registry.add("spring.redis.port", () -> redisContainer.getMappedPort(6379));
    }

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/auth";
        userRepository.deleteAll();
    }

    @Test
    void testAuth() {
        // 회원가입 요청
        RegisterRequest registerRequest = RegisterRequest.builder()
                .name("홍길동")
                .email("test@example.com")
                .password("password")
                .build();

        HttpEntity<RegisterRequest> registerEntity = new HttpEntity<>(registerRequest);

        ResponseEntity<RegisterResponse> registerResponse = restTemplate.postForEntity(baseUrl + "/register", registerEntity, RegisterResponse.class);

        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(registerResponse.getBody()).isNotNull();
        assertThat(registerResponse.getBody().getName()).isEqualTo("홍길동");
        assertThat(registerResponse.getBody().getEmail()).isEqualTo("test@example.com");

        // 로그인 요청
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password")
                .build();

        HttpEntity<LoginRequest> loginEntity = new HttpEntity<>(loginRequest);

        ResponseEntity<LoginResponse> loginResponse = restTemplate.postForEntity(baseUrl + "/login", loginEntity, LoginResponse.class);

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).isNotNull();
        assertThat(loginResponse.getBody().getAccessToken()).isNotBlank();

        // 액세스 토큰 갱신
        String accessToken = loginResponse.getBody().getAccessToken();
        String refreshToken = loginResponse.getBody().getRefreshToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        AccessTokenRequest tokenRequest = AccessTokenRequest.builder()
                .refreshToken(refreshToken)
                .build();

        HttpEntity<AccessTokenRequest> tokenEntity = new HttpEntity<>(tokenRequest, headers);

        ResponseEntity<AccessTokenResponse> tokenResponse = restTemplate.exchange(
                baseUrl + "/refresh",
                HttpMethod.POST,
                tokenEntity,
                AccessTokenResponse.class
        );
        assertThat(tokenResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(tokenResponse.getBody()).isNotNull();

        // 로그아웃 요청
        String reIssuedAccessToken = tokenResponse.getBody().getAccessToken();

        HttpHeaders logoutHeaders = new HttpHeaders();
        logoutHeaders.setContentType(MediaType.APPLICATION_JSON);
        logoutHeaders.setBearerAuth(reIssuedAccessToken);

        HttpEntity<Void> logoutEntity = new HttpEntity<>(logoutHeaders);

        ResponseEntity<Void> logoutResponse = restTemplate.exchange(
                baseUrl + "/logout",
                HttpMethod.POST,
                logoutEntity,
                Void.class
        );

        assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}