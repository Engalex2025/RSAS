CREATE DATABASE IF NOT EXISTS retaildb;
USE retaildb;


CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'ROLE_USER'
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

-- Security Events
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
    week INT DEFAULT 0
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
VALUES ('alexa', '$2a$10$qscZFRnqKj31nZgQ9vP7v.Q9vhWRhElY3Y2eRQHmhGbRGFb7LZouK', 'ROLE_ADMIN')
ON DUPLICATE KEY UPDATE password = VALUES(password), role = 'ROLE_ADMIN';

INSERT INTO users (username, password, role)
VALUES ('lucas', '$2a$10$K8TQpPXTxlz8eqtTMB9qSeNct14SmZr6aaB/9K0KwDw9Fb3pK10q2', 'ROLE_ADMIN')
ON DUPLICATE KEY UPDATE password = VALUES(password), role = 'ROLE_ADMIN';


UPDATE users SET role = 'ADMIN' WHERE username = 'alexa';
UPDATE users SET role = 'ADMIN' WHERE username = 'lucas';



UPDATE users SET role = 'ROLE_ADMIN' WHERE username = 'lucas';
SELECT username, role FROM users;

INSERT INTO products (product_id, name, quantity, minimum_quantity, price)
VALUES 
    ('R1001', 'Detergent', 150, 30, 2.99),
    ('R1002', 'Milk', 150, 40, 1.79),
    ('R1003', 'Shampoo', 50, 25, 4.99),
    ('R1004', 'Toilet Paper', 50, 30, 3.29),
    ('R1005', 'Bleach', 150, 40, 2.49),
    ('R1006', 'Yogurt', 50, 30, 1.29),
    ('R1007', 'Soap', 150, 35, 2.19),
    ('R1008', 'Cheese', 50, 30, 3.89)
AS new
ON DUPLICATE KEY UPDATE
    name = new.name,
    quantity = new.quantity,
    minimum_quantity = new.minimum_quantity,
    price = new.price;

CREATE OR REPLACE VIEW restock_logs_view AS
SELECT 
    restock_logs.id,
    restock_logs.product_id,
    products.name AS product_name,
    restock_logs.quantity_received,
    restock_logs.timestamp
FROM restock_logs
LEFT JOIN products ON restock_logs.product_id = products.product_id;

UPDATE products SET name = 'Toothbrush' WHERE product_id = 'R102';
UPDATE products SET name = 'Fabric Softener' WHERE product_id = 'R1023';
UPDATE products SET name = 'Bread' WHERE product_id = 'R102';



