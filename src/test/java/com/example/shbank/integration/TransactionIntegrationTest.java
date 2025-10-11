package com.example.shbank.integration;

import com.example.shbank.dto.transaction.*;
import com.example.shbank.entity.Account;
import com.example.shbank.entity.User;
import com.example.shbank.enums.AccountStatus;
import com.example.shbank.enums.AccountType;
import com.example.shbank.enums.TransactionType;
import com.example.shbank.repository.AccountRepository;
import com.example.shbank.repository.TransactionRepository;
import com.example.shbank.repository.UserRepository;
import com.example.shbank.util.JWTUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class TransactionIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

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
    private String jwtToken, recipientJwtToken;
    private User senderUser, recipientUser;
    private Account senderAccount, recipientAccount;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/transactions";
        transactionRepository.deleteAll();

        User testUser1 = User.builder()
                .email("test@example.com")
                .name("홍길동")
                .password(passwordEncoder.encode("password"))
                .build();

        senderUser = userRepository.save(testUser1);

        jwtToken = jwtUtil.generateAccessToken(senderUser.getId(), senderUser.getEmail());

        User testUser2 = User.builder()
                .email("test2@example.com")
                .name("홍하나")
                .password(passwordEncoder.encode("password"))
                .build();

        recipientUser = userRepository.save(testUser2);

        recipientJwtToken = jwtUtil.generateAccessToken(recipientUser.getId(), recipientUser.getEmail());

        Account testAccount1 = Account.builder()
                .user(senderUser)
                .accountName("테스트 계좌1")
                .type(AccountType.SAVINGS)
                .accountNumber("1234-567-890123")
                .balance(200000)
                .status(AccountStatus.ACTIVE)
                .authPassword(passwordEncoder.encode("123456"))
                .transferLimit(100000)
                .build();

        senderAccount = accountRepository.save(testAccount1);

        Account testAccount2 = Account.builder()
                .user(recipientUser)
                .accountName("테스트 계좌2")
                .type(AccountType.SAVINGS)
                .accountNumber("1234-567-890124")
                .balance(50000)
                .status(AccountStatus.ACTIVE)
                .authPassword(passwordEncoder.encode("123456"))
                .transferLimit(100000)
                .build();

        recipientAccount = accountRepository.save(testAccount2);
    }

    @Test
    void testTransaction() {
        // 즉시 송금
        TransactionRequest transactionRequest = TransactionRequest.builder()
                .senderAccountId(senderAccount.getId())
                .recipientAccountNumber("1234-567-890124")
                .type(TransactionType.TRANSFER_OUT)
                .amount(50000)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwtToken);

        HttpEntity<TransactionRequest> transactionEntity = new HttpEntity<>(transactionRequest, headers);

        ResponseEntity<TransactionResponse> transactionResponse = restTemplate.exchange(
                baseUrl + "/transfer",
                HttpMethod.POST,
                transactionEntity,
                TransactionResponse.class
        );

        assertThat(transactionResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(transactionResponse.getBody().getSenderName()).isEqualTo("홍길동");
        assertThat(transactionResponse.getBody().getRecipientName()).isEqualTo("홍하나");
        assertThat(transactionResponse.getBody().getAmount()).isEqualTo(50000);
        assertThat(transactionResponse.getBody().getBalance()).isEqualTo(150000);

        // 예약 송금
        TransactionRequest scheduleRequest = TransactionRequest.builder()
                .senderAccountId(senderAccount.getId())
                .recipientAccountNumber("1234-567-890124")
                .type(TransactionType.TRANSFER_OUT)
                .amount(50000)
                .memo("용돈")
                .scheduleDate(LocalDateTime.now().plusDays(1))
                .build();

        HttpEntity<TransactionRequest> scheduleEntity = new HttpEntity<>(scheduleRequest, headers);

        ResponseEntity<ScheduledTransferResponse> scheduleResponse = restTemplate.exchange(
                baseUrl + "/transfer",
                HttpMethod.POST,
                scheduleEntity,
                ScheduledTransferResponse.class
        );

        assertThat(scheduleResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(scheduleResponse.getBody().getRecipientName()).isEqualTo("홍하나");
        assertThat(scheduleResponse.getBody().getAmount()).isEqualTo(50000);
        assertThat(scheduleResponse.getBody().getType()).isEqualTo("TRANSFER_OUT");
        assertThat(scheduleResponse.getBody().getMemo()).isEqualTo("용돈");

        // 예약 송금 목록 조회
        HttpEntity<Void> getScheduleEntity = new HttpEntity<>(headers);

        ResponseEntity<List<ScheduledTransferResponse>> getScheduleResponse = restTemplate.exchange(
                baseUrl + "/" + senderAccount.getId() + "/scheduled",
                HttpMethod.GET,
                getScheduleEntity,
                new ParameterizedTypeReference<List<ScheduledTransferResponse>>() {}
        );

        assertThat(getScheduleResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<ScheduledTransferResponse> scheduledList = getScheduleResponse.getBody();
        assertThat(scheduledList).isNotNull();
        assertThat(scheduledList).hasSize(1);
        assertThat(scheduledList.get(0).getAmount()).isEqualTo(50000);
        assertThat(scheduledList.get(0).getRecipientName()).isEqualTo("홍하나");

        // 거래 내역 조회
        HttpHeaders recipientHeaders = new HttpHeaders();
        recipientHeaders.setContentType(MediaType.APPLICATION_JSON);
        recipientHeaders.setBearerAuth(recipientJwtToken);

        HttpEntity<Void> getTransactionEntity = new HttpEntity<>(recipientHeaders);

        ResponseEntity<TransactionHistoryResponse> getTransactionResponse = restTemplate.exchange(
                baseUrl + "/" + recipientAccount.getId(),
                HttpMethod.GET,
                getTransactionEntity,
                TransactionHistoryResponse.class
        );

        assertThat(getTransactionResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getTransactionResponse.getBody().getTotalIn()).isEqualTo(50000);
        assertThat(getTransactionResponse.getBody().getTotalOut()).isEqualTo(0);
        assertThat(getTransactionResponse.getBody().getNetChange()).isEqualTo(50000);
        List<TransactionResponse> transactionList = getTransactionResponse.getBody().getTransactions();
        assertThat(transactionList).hasSize(1);

        // 카드 결제
        CardPaymentRequest paymentRequest = CardPaymentRequest.builder()
                .accountId(senderAccount.getId())
                .amount(100000)
                .merchantName("스타벅스")
                .build();

        HttpEntity<CardPaymentRequest> paymentEntity = new HttpEntity<>(paymentRequest, headers);

        ResponseEntity<CardPaymentResponse> paymentResponse = restTemplate.exchange(
                baseUrl + "/card-payment",
                HttpMethod.POST,
                paymentEntity,
                CardPaymentResponse.class
        );

        assertThat(paymentResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(paymentResponse.getBody().getBalance()).isEqualTo(0);
        assertThat(paymentResponse.getBody().getMerchantName()).isEqualTo("스타벅스");
        assertThat(paymentResponse.getBody().getAmount()).isEqualTo(100000);
        assertThat(paymentResponse.getBody().getSenderName()).isEqualTo("홍길동");
    }
}