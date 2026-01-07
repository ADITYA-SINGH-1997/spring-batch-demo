package com.example.batchbackup.model.customer;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "customers_backup")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerBackup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_id", nullable = false)
    private Long originalId;

    @Column(name = "customer_code", nullable = false)
    private String customerCode;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "last_activity_date")
    private LocalDateTime lastActivityDate;

    @Column(name = "backup_date", nullable = false)
    private LocalDateTime backupDate;
}
