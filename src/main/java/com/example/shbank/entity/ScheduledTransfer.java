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
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Transaction transaction;

    private LocalDateTime scheduleDate;
    private String memo;
}
