package com.example.batchbackup.repository.product;

import com.example.batchbackup.model.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE p.status = 'DISCONTINUED' AND p.updatedDate < :cutoffDate")
    List<Product> findDiscontinuedProducts(@Param("cutoffDate") LocalDateTime cutoffDate);
}
