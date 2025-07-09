
CREATE DATABASE IF NOT EXISTS retaildb;
USE retaildb;

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

INSERT INTO products (id,product_id,name,quantity,minimum_quantity,price)
VALUES
(1,"R1001","EcoBrush",25,5,4.99),
(2,"R1002","SolarLamp Mini",15,4,9.5),
(3,"R1003","Foldable Cup",30,5,3.75),
(4,"R1004","SmartKey Tag",18,3,11.9),
(5,"R1005","HydraBottle",10,5,36.8),
(6,"R1006","Kinder Bueno",40,10,7.25),
(7,"R1007","Broom",5,5,12.4),
(8,"R1008","ToothBrush",8,2,15.99),
(9,"R1009","Phone Charger",22,6,14.3),
(10,"R1010","SuperGlue",16,3,2.99);

INSERT INTO restock_logs (product_id, quantity_received, timestamp)
VALUES
("R1002",5,'26/06/2025 18:05'),
("R1003",10,'25/06/2025 18:05'),
("R1009",7,'05/06/2025 18:05'),
("R1004",9,'22/06/2025 18:05'),
("R1006",3,'01/07/2025 18:05'),
("R1006",4,'22/06/2025 18:05'),
("R1001",3,'28/06/2025 18:05'),
("R1003",1,'22/06/2025 18:05'),
("R1004",3,'01/07/2025 18:05'),
("R1007",8,'26/06/2025 18:05');

INSERT INTO security_events (camera_id,detected_behavior,alert_level,message,location,event_time)
VALUES
("CAM-000","intruder alert","LOW","Potential threat","Zone A",'2025-07-01 07:05'),
("CAM-001","intruder alert","MEDIUM","Potential threat","Zone B",'2025-07-05 12:05'),
("CAM-002","normal activity","HIGH","Potential threat","Zone C",'2025-07-11 03:05'),
("CAM-003","loitering","MEDIUM","Immediate action required","Zone D",'2025-06-25 20:05'),
("CAM-004","intruder alert","MEDIUM","Check this zone","Zone E",'2024-07-24 01:05'),
("CAM-005","suspicious movement","HIGH", "Immediate action required","Zone F", '2022-07-15 12:05'),
("CAM-006","loitering","MEDIUM","All clear","Zone G",'2025-07-25 16:05'),
("CAM-007","normal activity","LOW","All clear","Zone H",'2025-07-16 06:05'),
("CAM-008","loitering","HIGH","Potential threat","Zone I",'2025-06-30 19:05'),
("CAM-009","loitering","HIGH","Check this zone","Zone J",'2025-07-12 07:05');

INSERT INTO sales_data (area_code,product_id,quantity_sold)
VALUES
('A101',"R1001",3),
('A102',"R1002",5),
('A103',"R1003",2),
('A104',"R1004",1),
('A105',"R1005",6),
('A106',"R1006",4),
('A107',"R1007",3),
('A108',"R1008",5),
('A109',"R1009",7),
('A110',"R1010",2);

INSERT INTO price_updates (product_id,old_price,new_price,updated_by)
VALUES
('R1001',4.99,4.79,"Alex-admin"),
('R1002',9.5,9.2,"Alex-admin"),
('R1003',6.75,3.99,"Alex-admin"),
('R1004',11.9,12.1,"Alex-admin"),
('R1005',6.8,6.5,"Alex-admin"),
('R1006',7.25,7.15,"Alex-admin"),
('R1007',12.4,12,"Alex-admin"),
('R1008',15.99,15.49,"Alex-admin"),
('R1009',14.3,14.99,"Alex-admin"),
('R1010',2.99,3.1,"Alex-admin");

