package com.example.batchbackup.config;

import com.example.batchbackup.model.customer.Customer;
import com.example.batchbackup.model.customer.CustomerBackup;
import com.example.batchbackup.model.order.Order;
import com.example.batchbackup.model.order.OrderBackup;
import com.example.batchbackup.model.product.Product;
import com.example.batchbackup.model.product.ProductBackup;
import com.example.batchbackup.repository.customer.CustomerBackupRepository;
import com.example.batchbackup.repository.customer.CustomerRepository;
import com.example.batchbackup.repository.order.OrderBackupRepository;
import com.example.batchbackup.repository.order.OrderRepository;
import com.example.batchbackup.repository.product.ProductBackupRepository;
import com.example.batchbackup.repository.product.ProductRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class BatchConfiguration {

    @Value("${backup.retention.days:90}")
    private int retentionDays;

    private final OrderRepository orderRepository;
    private final OrderBackupRepository orderBackupRepository;
    private final CustomerRepository customerRepository;
    private final CustomerBackupRepository customerBackupRepository;
    private final ProductRepository productRepository;
    private final ProductBackupRepository productBackupRepository;

    public BatchConfiguration(
            OrderRepository orderRepository,
            OrderBackupRepository orderBackupRepository,
            CustomerRepository customerRepository,
            CustomerBackupRepository customerBackupRepository,
            ProductRepository productRepository,
            ProductBackupRepository productBackupRepository) {
        this.orderRepository = orderRepository;
        this.orderBackupRepository = orderBackupRepository;
        this.customerRepository = customerRepository;
        this.customerBackupRepository = customerBackupRepository;
        this.productRepository = productRepository;
        this.productBackupRepository = productBackupRepository;
    }

    // Order Backup Job
    @Bean
    public Job orderBackupJob(JobRepository jobRepository, Step orderBackupStep) {
        return new JobBuilder("orderBackupJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(orderBackupStep)
                .build();
    }

    @Bean
    public Step orderBackupStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<Order> orderReader,
            ItemProcessor<Order, OrderBackup> orderProcessor,
            ItemWriter<OrderBackup> orderWriter) {
        return new StepBuilder("orderBackupStep", jobRepository)
                .<Order, OrderBackup>chunk(100, transactionManager)
                .reader(orderReader)
                .processor(orderProcessor)
                .writer(orderWriter)
                .build();
    }

    @Bean
    public ItemReader<Order> orderReader() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        List<Order> oldOrders = orderRepository.findOldOrders(cutoffDate);
        return new ListItemReader<>(oldOrders);
    }

    @Bean
    public ItemProcessor<Order, OrderBackup> orderProcessor() {
        return order -> {
            OrderBackup backup = new OrderBackup();
            backup.setOriginalId(order.getId());
            backup.setOrderNumber(order.getOrderNumber());
            backup.setCustomerId(order.getCustomerId());
            backup.setProductId(order.getProductId());
            backup.setQuantity(order.getQuantity());
            backup.setTotalAmount(order.getTotalAmount());
            backup.setStatus(order.getStatus());
            backup.setCreatedDate(order.getCreatedDate());
            backup.setUpdatedDate(order.getUpdatedDate());
            backup.setBackupDate(LocalDateTime.now());
            return backup;
        };
    }

    @Bean
    public ItemWriter<OrderBackup> orderWriter() {
        return items -> orderBackupRepository.saveAll(items);
    }

    // Customer Backup Job
    @Bean
    public Job customerBackupJob(JobRepository jobRepository, Step customerBackupStep) {
        return new JobBuilder("customerBackupJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(customerBackupStep)
                .build();
    }

    @Bean
    public Step customerBackupStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<Customer> customerReader,
            ItemProcessor<Customer, CustomerBackup> customerProcessor,
            ItemWriter<CustomerBackup> customerWriter) {
        return new StepBuilder("customerBackupStep", jobRepository)
                .<Customer, CustomerBackup>chunk(100, transactionManager)
                .reader(customerReader)
                .processor(customerProcessor)
                .writer(customerWriter)
                .build();
    }

    @Bean
    public ItemReader<Customer> customerReader() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        List<Customer> inactiveCustomers = customerRepository.findInactiveCustomers(cutoffDate);
        return new ListItemReader<>(inactiveCustomers);
    }

    @Bean
    public ItemProcessor<Customer, CustomerBackup> customerProcessor() {
        return customer -> {
            CustomerBackup backup = new CustomerBackup();
            backup.setOriginalId(customer.getId());
            backup.setCustomerCode(customer.getCustomerCode());
            backup.setFirstName(customer.getFirstName());
            backup.setLastName(customer.getLastName());
            backup.setEmail(customer.getEmail());
            backup.setPhoneNumber(customer.getPhoneNumber());
            backup.setStatus(customer.getStatus());
            backup.setCreatedDate(customer.getCreatedDate());
            backup.setLastActivityDate(customer.getLastActivityDate());
            backup.setBackupDate(LocalDateTime.now());
            return backup;
        };
    }

    @Bean
    public ItemWriter<CustomerBackup> customerWriter() {
        return items -> customerBackupRepository.saveAll(items);
    }

    // Product Backup Job
    @Bean
    public Job productBackupJob(JobRepository jobRepository, Step productBackupStep) {
        return new JobBuilder("productBackupJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(productBackupStep)
                .build();
    }

    @Bean
    public Step productBackupStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<Product> productReader,
            ItemProcessor<Product, ProductBackup> productProcessor,
            ItemWriter<ProductBackup> productWriter) {
        return new StepBuilder("productBackupStep", jobRepository)
                .<Product, ProductBackup>chunk(100, transactionManager)
                .reader(productReader)
                .processor(productProcessor)
                .writer(productWriter)
                .build();
    }

    @Bean
    public ItemReader<Product> productReader() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        List<Product> discontinuedProducts = productRepository.findDiscontinuedProducts(cutoffDate);
        return new ListItemReader<>(discontinuedProducts);
    }

    @Bean
    public ItemProcessor<Product, ProductBackup> productProcessor() {
        return product -> {
            ProductBackup backup = new ProductBackup();
            backup.setOriginalId(product.getId());
            backup.setProductCode(product.getProductCode());
            backup.setProductName(product.getProductName());
            backup.setDescription(product.getDescription());
            backup.setPrice(product.getPrice());
            backup.setStockQuantity(product.getStockQuantity());
            backup.setCategory(product.getCategory());
            backup.setStatus(product.getStatus());
            backup.setCreatedDate(product.getCreatedDate());
            backup.setUpdatedDate(product.getUpdatedDate());
            backup.setBackupDate(LocalDateTime.now());
            return backup;
        };
    }

    @Bean
    public ItemWriter<ProductBackup> productWriter() {
        return items -> productBackupRepository.saveAll(items);
    }
}
