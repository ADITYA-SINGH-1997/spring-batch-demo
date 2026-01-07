package com.example.batchbackup.repository.customer;

import com.example.batchbackup.model.customer.CustomerBackup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerBackupRepository extends JpaRepository<CustomerBackup, Long> {
}
