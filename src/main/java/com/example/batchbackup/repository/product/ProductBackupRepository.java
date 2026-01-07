package com.example.batchbackup.repository.product;

import com.example.batchbackup.model.product.ProductBackup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductBackupRepository extends JpaRepository<ProductBackup, Long> {
}
