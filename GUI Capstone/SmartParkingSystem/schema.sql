-- =====================================================================
--  Smart Parking Management System - Database Schema
--  Run this entire script in phpMyAdmin (SQL tab) on a fresh database
--  named "parking_system"
-- =====================================================================

CREATE DATABASE IF NOT EXISTS parking_system;
USE parking_system;

-- ---------------------------------------------------------------------
-- Table: users
-- Stores both Admin and Customer accounts.
-- Passwords are stored as SHA-256 hashes, never plain text.
-- ---------------------------------------------------------------------
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,      -- SHA-256 hash (64 hex chars)
    role ENUM('Admin', 'Customer') NOT NULL DEFAULT 'Customer',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Table: parking_slots
-- ---------------------------------------------------------------------
CREATE TABLE parking_slots (
    slot_id INT AUTO_INCREMENT PRIMARY KEY,
    slot_number VARCHAR(10) NOT NULL UNIQUE,
    status ENUM('Available', 'Occupied') NOT NULL DEFAULT 'Available',
    rate_per_hour DECIMAL(8,2) NOT NULL DEFAULT 20.00
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Table: reservations
-- entry_time / exit_time are nullable: exit_time is filled in when the
-- customer releases the slot. total_amount is computed at release time.
-- ---------------------------------------------------------------------
CREATE TABLE reservations (
    reservation_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    slot_id INT NOT NULL,
    entry_time DATETIME NOT NULL,
    exit_time DATETIME NULL,
    total_amount DECIMAL(10,2) NULL,
    CONSTRAINT fk_reservation_user FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_reservation_slot FOREIGN KEY (slot_id) REFERENCES parking_slots(slot_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
--  Seed Data
-- =====================================================================

-- Default Admin account -> username: admin   | password: admin123
-- Default Customer account -> username: john  | password: john123
-- (Password hashes below are SHA-256 of the plain text passwords above)
INSERT INTO users (username, password, role) VALUES
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Admin'),
('john',  'b4b597c714a8f49103da4dab0266af0ee0ae4f8575250a84855c3d76941cd422', 'Customer');

-- Default parking slots
INSERT INTO parking_slots (slot_number, status, rate_per_hour) VALUES
('A1', 'Available', 20.00),
('A2', 'Available', 20.00),
('A3', 'Occupied', 20.00),
('B1', 'Available', 30.00),
('B2', 'Available', 30.00);
