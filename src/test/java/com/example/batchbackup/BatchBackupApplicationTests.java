package com.example.batchbackup;

import com.example.batchbackup.model.order.Order;
import com.example.batchbackup.model.order.OrderBackup;
import com.example.batchbackup.repository.order.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class BatchBackupApplicationTests {

    @Autowired(required = false)
    private JobLauncher jobLauncher;

    @Autowired(required = false)
    @Qualifier("orderBackupJob")
    private Job orderBackupJob;

    @Autowired(required = false)
    private OrderRepository orderRepository;

    @Test
    void contextLoads() {
        // Just verify the Spring context loads successfully
        assertNotNull(orderBackupJob);
    }

    @Test
    void testOrderEntityCreation() {
        // Test that we can create an order entity
        Order order = new Order();
        order.setOrderNumber("TEST-001");
        order.setCustomerId(1L);
        order.setProductId(1L);
        order.setQuantity(2);
        order.setTotalAmount(new BigDecimal("99.99"));
        order.setStatus("COMPLETED");
        order.setCreatedDate(LocalDateTime.now().minusDays(100));
        order.setUpdatedDate(LocalDateTime.now().minusDays(100));

        assertNotNull(order);
        assertNotNull(order.getOrderNumber());
    }

    @Test
    void testOrderBackupEntityCreation() {
        // Test that we can create an order backup entity
        OrderBackup backup = new OrderBackup();
        backup.setOriginalId(1L);
        backup.setOrderNumber("TEST-001");
        backup.setCustomerId(1L);
        backup.setProductId(1L);
        backup.setQuantity(2);
        backup.setTotalAmount(new BigDecimal("99.99"));
        backup.setStatus("COMPLETED");
        backup.setCreatedDate(LocalDateTime.now().minusDays(100));
        backup.setUpdatedDate(LocalDateTime.now().minusDays(100));
        backup.setBackupDate(LocalDateTime.now());

        assertNotNull(backup);
        assertNotNull(backup.getBackupDate());
    }
}
