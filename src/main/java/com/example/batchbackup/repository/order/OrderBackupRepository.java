package com.example.batchbackup.repository.order;

import com.example.batchbackup.model.order.OrderBackup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderBackupRepository extends JpaRepository<OrderBackup, Long> {
}
