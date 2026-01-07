USE batch_backup_db;

-- Insert sample customers
INSERT INTO customers (customer_code, first_name, last_name, email, phone_number, status, created_date, last_activity_date) VALUES
('CUST001', 'John', 'Doe', 'john.doe@email.com', '1234567890', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 50 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY)),
('CUST002', 'Jane', 'Smith', 'jane.smith@email.com', '1234567891', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 100 DAY), DATE_SUB(NOW(), INTERVAL 95 DAY)),
('CUST003', 'Bob', 'Johnson', 'bob.johnson@email.com', '1234567892', 'INACTIVE', DATE_SUB(NOW(), INTERVAL 150 DAY), DATE_SUB(NOW(), INTERVAL 100 DAY)),
('CUST004', 'Alice', 'Williams', 'alice.williams@email.com', '1234567893', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 200 DAY), DATE_SUB(NOW(), INTERVAL 120 DAY)),
('CUST005', 'Charlie', 'Brown', 'charlie.brown@email.com', '1234567894', 'INACTIVE', DATE_SUB(NOW(), INTERVAL 250 DAY), DATE_SUB(NOW(), INTERVAL 180 DAY)),
('CUST006', 'David', 'Davis', 'david.davis@email.com', '1234567895', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 300 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
('CUST007', 'Emma', 'Miller', 'emma.miller@email.com', '1234567896', 'INACTIVE', DATE_SUB(NOW(), INTERVAL 350 DAY), DATE_SUB(NOW(), INTERVAL 200 DAY)),
('CUST008', 'Frank', 'Wilson', 'frank.wilson@email.com', '1234567897', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
('CUST009', 'Grace', 'Moore', 'grace.moore@email.com', '1234567898', 'INACTIVE', DATE_SUB(NOW(), INTERVAL 400 DAY), DATE_SUB(NOW(), INTERVAL 250 DAY)),
('CUST010', 'Henry', 'Taylor', 'henry.taylor@email.com', '1234567899', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 60 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY));

-- Insert sample products
INSERT INTO products (product_code, product_name, description, price, stock_quantity, category, status, created_date, updated_date) VALUES
('PROD001', 'Laptop', 'High-performance laptop', 999.99, 50, 'Electronics', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 100 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY)),
('PROD002', 'Mouse', 'Wireless mouse', 29.99, 200, 'Electronics', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 120 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY)),
('PROD003', 'Keyboard', 'Mechanical keyboard', 79.99, 150, 'Electronics', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 80 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
('PROD004', 'Monitor', '27-inch 4K monitor', 349.99, 75, 'Electronics', 'DISCONTINUED', DATE_SUB(NOW(), INTERVAL 200 DAY), DATE_SUB(NOW(), INTERVAL 100 DAY)),
('PROD005', 'Desk Chair', 'Ergonomic office chair', 199.99, 30, 'Furniture', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 150 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY)),
('PROD006', 'Desk Lamp', 'LED desk lamp', 39.99, 100, 'Furniture', 'DISCONTINUED', DATE_SUB(NOW(), INTERVAL 250 DAY), DATE_SUB(NOW(), INTERVAL 150 DAY)),
('PROD007', 'Notebook', 'A4 spiral notebook', 5.99, 500, 'Stationery', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 90 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY)),
('PROD008', 'Pen Set', 'Set of 10 pens', 12.99, 300, 'Stationery', 'DISCONTINUED', DATE_SUB(NOW(), INTERVAL 300 DAY), DATE_SUB(NOW(), INTERVAL 200 DAY)),
('PROD009', 'Water Bottle', 'Stainless steel water bottle', 24.99, 150, 'Accessories', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 70 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
('PROD010', 'Backpack', 'Laptop backpack', 59.99, 80, 'Accessories', 'DISCONTINUED', DATE_SUB(NOW(), INTERVAL 350 DAY), DATE_SUB(NOW(), INTERVAL 250 DAY));

-- Insert sample orders
INSERT INTO orders (order_number, customer_id, product_id, quantity, total_amount, status, created_date, updated_date) VALUES
('ORD001', 1, 1, 1, 999.99, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 40 DAY), DATE_SUB(NOW(), INTERVAL 39 DAY)),
('ORD002', 2, 2, 2, 59.98, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 95 DAY), DATE_SUB(NOW(), INTERVAL 94 DAY)),
('ORD003', 3, 3, 1, 79.99, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 100 DAY), DATE_SUB(NOW(), INTERVAL 99 DAY)),
('ORD004', 4, 4, 1, 349.99, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 120 DAY), DATE_SUB(NOW(), INTERVAL 119 DAY)),
('ORD005', 5, 5, 1, 199.99, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 180 DAY), DATE_SUB(NOW(), INTERVAL 179 DAY)),
('ORD006', 1, 7, 5, 29.95, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 19 DAY)),
('ORD007', 6, 9, 2, 49.98, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
('ORD008', 7, 6, 1, 39.99, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 200 DAY), DATE_SUB(NOW(), INTERVAL 199 DAY)),
('ORD009', 8, 2, 3, 89.97, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY)),
('ORD010', 9, 8, 2, 25.98, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 250 DAY), DATE_SUB(NOW(), INTERVAL 249 DAY)),
('ORD011', 10, 3, 1, 79.99, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 29 DAY)),
('ORD012', 1, 9, 1, 24.99, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY)),
('ORD013', 3, 10, 1, 59.99, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 100 DAY), DATE_SUB(NOW(), INTERVAL 99 DAY)),
('ORD014', 5, 4, 2, 699.98, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 180 DAY), DATE_SUB(NOW(), INTERVAL 179 DAY)),
('ORD015', 7, 6, 3, 119.97, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 200 DAY), DATE_SUB(NOW(), INTERVAL 199 DAY));
