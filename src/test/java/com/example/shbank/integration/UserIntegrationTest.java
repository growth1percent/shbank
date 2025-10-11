package com.example.shbank.integration;
import com.example.shbank.dto.user.MyInfoResponse;
import com.example.shbank.dto.user.MyInfoUpdateRequest;
import com.example.shbank.dto.user.MyInfoUpdateResponse;
import com.example.shbank.entity.User;
import com.example.shbank.repository.UserRepository;
import com.example.shbank.util.JWTUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class UserIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
    private User savedUser;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/users";

        User testUser = User.builder()
                .name("홍길동")
                .email("test@example.com")
                .password(passwordEncoder.encode("password"))
                .build();

        savedUser = userRepository.save(testUser);

        jwtToken = jwtUtil.generateAccessToken(savedUser.getId(), savedUser.getEmail());
    }

    @Test
    void testUser() {
        // 내 정보 조회
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<Void> getRequest = new HttpEntity<>(headers);

        ResponseEntity<MyInfoResponse> getResponse = restTemplate.exchange(
                baseUrl + "/me",
                HttpMethod.GET,
                getRequest,
                MyInfoResponse.class
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getEmail()).isEqualTo(savedUser.getEmail());
        assertThat(getResponse.getBody().getName()).isEqualTo(savedUser.getName());

        // 내 정보 수정
        MyInfoUpdateRequest updateRequest = MyInfoUpdateRequest.builder()
                .email("update@example.com")
                .build();

        HttpEntity<MyInfoUpdateRequest> patchRequest = new HttpEntity<>(updateRequest, headers);

        ResponseEntity<MyInfoUpdateResponse> patchResponse = restTemplate.exchange(
                baseUrl + "/me",
                HttpMethod.PATCH,
                patchRequest,
                MyInfoUpdateResponse.class
        );

        assertThat(patchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(patchResponse.getBody()).isNotNull();
        assertThat(patchResponse.getBody().getEmail()).isEqualTo("update@example.com");

        // DB 반영 확인
        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updatedUser.getEmail()).isEqualTo("update@example.com");
    }
}