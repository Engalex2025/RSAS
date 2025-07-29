CREATE DATABASE IF NOT EXISTS retaildb;
USE retaildb;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'USER'
);

CREATE TABLE IF NOT EXISTS products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100),
    quantity INT DEFAULT 0,
    minimum_quantity INT DEFAULT 5,
    price DECIMAL(10,2),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS restock_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id VARCHAR(50),
    quantity_received INT,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

CREATE TABLE IF NOT EXISTS security_events (
    id INT AUTO_INCREMENT PRIMARY KEY,
    camera_id VARCHAR(50),
    detected_behavior VARCHAR(255),
    alert_level VARCHAR(50),
    message TEXT,
    location VARCHAR(100),
    event_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sales_data (
    id INT AUTO_INCREMENT PRIMARY KEY,
    area_code VARCHAR(20),
    product_id VARCHAR(50),
    product_name VARCHAR(100),
    category VARCHAR(100),
    quantity_sold INT,
    sale_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS price_updates (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id VARCHAR(50),
    old_price DECIMAL(10,2),
    new_price DECIMAL(10,2),
    updated_by VARCHAR(100),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

CREATE TABLE IF NOT EXISTS relocation_suggestions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id VARCHAR(50),
    product_name VARCHAR(100),
    category VARCHAR(100),
    from_area VARCHAR(20),
    to_area VARCHAR(20),
    reason TEXT,
    suggestion_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO sales_data (area_code, product_id, product_name, category, quantity_sold)
VALUES
('A101', 'R001', 'SoapX', 'Hygiene', 120),
('B202', 'R002', 'DetergentMax', 'Cleaning', 80),
('C303', 'R003', 'YogurtZ', 'Dairy', 30),
('D404', 'R004', 'ToothpasteZ', 'Hygiene', 55),
('E505', 'R005', 'FloorShine', 'Cleaning', 90),
('A101', 'R006', 'MilkY', 'Dairy', 15);

INSERT INTO users (username, password, role)
VALUES ('alexa', '$2a$10$qscZFRnqKj31nZgQ9vP7v.Q9vhWRhElY3Y2eRQHmhGbRGFb7LZouK', 'ADMIN')
ON DUPLICATE KEY UPDATE password = VALUES(password), role = 'ADMIN';

INSERT INTO users (username, password, role)
VALUES ('lucas', '$2a$10$K8TQpPXTxlz8eqtTMB9qSeNct14SmZr6aaB/9K0KwDw9Fb3pK10q2', 'ROLE_ADMIN')
ON DUPLICATE KEY UPDATE password = VALUES(password), role = 'ROLE_ADMIN';
