-- CampusLink Career Database Schema
-- Run this script to set up the database

CREATE DATABASE IF NOT EXISTS campuslink_career;
USE campuslink_career;

-- Disable foreign key checks to allow dropping tables in any order
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS resources;
DROP TABLE IF EXISTS applications;
DROP TABLE IF EXISTS internships;
DROP TABLE IF EXISTS jobs;
DROP TABLE IF EXISTS employers;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

-- Users table (authentication)
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'STUDENT', 'EMPLOYER') NOT NULL,
    must_change_password BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Students table
CREATE TABLE students (
    student_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    course VARCHAR(100),
    year_of_study INT,
    skills TEXT,
    cv_path VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX (user_id)
);

-- Employers table
CREATE TABLE employers (
    employer_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    company_name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100),
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    address VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX (user_id)
);

-- Jobs table
CREATE TABLE jobs (
    job_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    requirements TEXT,
    deadline DATE,
    employer_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employer_id) REFERENCES employers(employer_id) ON DELETE SET NULL,
    INDEX (employer_id),
    INDEX (deadline)
);

-- Internships table
CREATE TABLE internships (
    internship_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    requirements TEXT,
    deadline DATE,
    employer_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employer_id) REFERENCES employers(employer_id) ON DELETE SET NULL,
    INDEX (employer_id),
    INDEX (deadline)
);

-- Applications table
CREATE TABLE applications (
    application_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    opportunity_type ENUM('JOB', 'INTERNSHIP') NOT NULL,
    opportunity_id INT NOT NULL,
    application_date DATE NOT NULL,
    status ENUM('PENDING', 'REVIEWED', 'ACCEPTED', 'REJECTED', 'WITHDRAWN') DEFAULT 'PENDING',
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    UNIQUE KEY unique_application (student_id, opportunity_type, opportunity_id),
    INDEX (opportunity_id, opportunity_type)
);

-- Resources table
CREATE TABLE resources (
    resource_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    file_path VARCHAR(255),
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Audit log: append-only.
CREATE TABLE audit_log (
    audit_id INT PRIMARY KEY AUTO_INCREMENT,
    event_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    event VARCHAR(50) NOT NULL,
    username VARCHAR(50),
    outcome VARCHAR(20) NOT NULL,
    details VARCHAR(255),
    INDEX (username),
    INDEX (event_time)
);

-- Seed default admin account
-- Username: admin, Password: Admin@123
INSERT INTO users (username, password, role, must_change_password) 
VALUES ('admin', '$2a$10$QfOqlbdtY0RYankmYG2SxOIaCp5zVqrTiSLUAdu2cMouWMBPaDZye', 'ADMIN', TRUE);

-- Sample resources
INSERT IGNORE INTO resources (title, description, file_path) VALUES
('CV Writing Guide', 'Comprehensive guide to writing a professional CV for university graduates.', '/resources/cv_writing_guide.pdf'),
('Interview Tips', 'Essential tips and techniques for acing your job interviews.', '/resources/interview_tips.pdf'),
('Career Development Roadmap', 'Step-by-step career development guide for technology professionals.', '/resources/career_roadmap.pdf');
