package com.example.batchbackup.repository.customer;

import com.example.batchbackup.model.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("SELECT c FROM Customer c WHERE c.lastActivityDate < :cutoffDate")
    List<Customer> findInactiveCustomers(@Param("cutoffDate") LocalDateTime cutoffDate);
}
