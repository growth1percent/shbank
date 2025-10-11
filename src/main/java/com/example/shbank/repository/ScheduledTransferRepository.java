package com.example.shbank.repository;

import com.example.shbank.entity.ScheduledTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduledTransferRepository extends JpaRepository<ScheduledTransfer, Long> {
}
