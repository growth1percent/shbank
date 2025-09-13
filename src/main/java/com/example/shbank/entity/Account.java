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
}
