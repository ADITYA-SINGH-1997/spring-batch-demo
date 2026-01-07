-- Create database
CREATE DATABASE IF NOT EXISTS batch_backup_db;
USE batch_backup_db;

-- Orders table
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_date DATETIME NOT NULL,
    updated_date DATETIME,
    INDEX idx_created_date (created_date)
);

-- Orders backup table
CREATE TABLE IF NOT EXISTS orders_backup (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_id BIGINT NOT NULL,
    order_number VARCHAR(50) NOT NULL,
    customer_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_date DATETIME NOT NULL,
    updated_date DATETIME,
    backup_date DATETIME NOT NULL,
    INDEX idx_backup_date (backup_date)
);

-- Customers table
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_code VARCHAR(50) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    status VARCHAR(20) NOT NULL,
    created_date DATETIME NOT NULL,
    last_activity_date DATETIME,
    INDEX idx_last_activity_date (last_activity_date)
);

-- Customers backup table
CREATE TABLE IF NOT EXISTS customers_backup (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_id BIGINT NOT NULL,
    customer_code VARCHAR(50) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    phone_number VARCHAR(20),
    status VARCHAR(20) NOT NULL,
    created_date DATETIME NOT NULL,
    last_activity_date DATETIME,
    backup_date DATETIME NOT NULL,
    INDEX idx_backup_date (backup_date)
);

-- Products table
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_code VARCHAR(50) NOT NULL UNIQUE,
    product_name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock_quantity INT NOT NULL,
    category VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_date DATETIME NOT NULL,
    updated_date DATETIME,
    INDEX idx_status_updated_date (status, updated_date)
);

-- Products backup table
CREATE TABLE IF NOT EXISTS products_backup (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_id BIGINT NOT NULL,
    product_code VARCHAR(50) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock_quantity INT NOT NULL,
    category VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_date DATETIME NOT NULL,
    updated_date DATETIME,
    backup_date DATETIME NOT NULL,
    INDEX idx_backup_date (backup_date)
);
