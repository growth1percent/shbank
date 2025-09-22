package com.example.shbank.entity;

import com.example.shbank.common.BaseEntity;
import com.example.shbank.enums.AccountStatus;
import com.example.shbank.enums.AccountType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "accounts")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @Column(nullable = false)
    private String accountName;

    @Builder.Default
    @Column(nullable = false)
    private Integer balance = 0;

    @Column
    private Integer transferLimit;

    @Column(nullable = false)
    private String authPassword;

    @Builder.Default
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;

    public Account assignAccountNumberFromId() {
        if (!"temp".equals(this.accountNumber)) {
            throw new IllegalStateException("이미 계좌번호가 설정되었습니다.");
        }

        // ID 기반으로 고유한 계좌 번호 생성
        long idValue = this.id;

        int middle = (int) ((idValue / 1000000) % 1000);
        int last = (int) (idValue % 1000000);

        this.accountNumber = String.format("1234-%03d-%06d", middle, last);
        return this;
    }

    public void updateTransferLimit(Integer transferLimit) {
        this.transferLimit = transferLimit;
    }

    public void updateAuthPassword(String authPassword) {
        this.authPassword = authPassword;
    }

    public void deposit(Integer amount) {
        this.balance += amount;
    }

    public void withdraw(Integer amount) {
        if (this.balance < amount) throw new IllegalStateException("잔액 부족");
        this.balance -= amount;
    }
}
