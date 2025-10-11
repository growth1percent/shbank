package com.example.shbank.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "scheduled_transfers")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class ScheduledTransfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false, unique = true)
    private Transaction transaction;

    private LocalDateTime scheduleDate;
    private String memo;

    public void linkTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
}
