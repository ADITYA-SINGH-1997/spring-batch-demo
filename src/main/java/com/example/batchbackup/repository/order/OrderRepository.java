package com.example.batchbackup.repository.order;

import com.example.batchbackup.model.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o WHERE o.createdDate < :cutoffDate")
    List<Order> findOldOrders(@Param("cutoffDate") LocalDateTime cutoffDate);
}
