package com.example.shbank.entity;

import com.example.shbank.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_account_id", nullable = false)
    private Account senderAccount;

    @ManyToOne(fetch = FetchType.LAZY) // 카드 결제 때는 null 가능
    @JoinColumn(name = "recipient_account_id")
    private Account recipientAccount;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    private Integer balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime transactionDate;

    @OneToOne(mappedBy = "transaction", cascade = CascadeType.ALL, optional = true)
    private ScheduledTransfer scheduledTransfer;

    @OneToOne(mappedBy = "transaction", cascade = CascadeType.ALL, optional = true)
    private CardPayment cardPayment;
}



