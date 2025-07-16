
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

INSERT INTO users (username, password, role) VALUES (
  'alexa',
  '$2a$10$0vvYl7aZyzAaTWNoz5.14OQK1yH6sDMP7S5ZoMTWJfljwH4d8/I0e',
  'ADMIN'
);
UPDATE users
SET password = '$2a$10$0vvYl7aZyzAaTWNoz5.14OQK1yH6sDMP7S5ZoMTWJfljwH4d8/I0e',
    role = 'ADMIN'
WHERE username = 'alexa';
SELECT * FROM users;
DELETE FROM users WHERE username = 'admin';
SELECT * FROM users;
DELETE FROM users WHERE username = 'alexa';

INSERT INTO users (username, password, role)
VALUES (
  'alexa',
  '$2a$10$qscZFRnqKj31nZgQ9vP7v.Q9vhWRhElY3Y2eRQHmhGbRGFb7LZouK',
  'ADMIN'
);





