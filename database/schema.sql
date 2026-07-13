-- CampusLink Career Database Schema
-- Run this script to set up the database

CREATE DATABASE IF NOT EXISTS campuslink_career;
USE campuslink_career;

-- Users table (authentication)
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Students table
CREATE TABLE IF NOT EXISTS students (
    student_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    course VARCHAR(100),
    year_of_study INT,
    skills TEXT,
    cv_path VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Employers table
CREATE TABLE IF NOT EXISTS employers (
    employer_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    company_name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100),
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    address VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Jobs table
CREATE TABLE IF NOT EXISTS jobs (
    job_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    requirements TEXT,
    deadline DATE,
    employer_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employer_id) REFERENCES employers(employer_id) ON DELETE SET NULL
);

-- Internships table
CREATE TABLE IF NOT EXISTS internships (
    internship_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    requirements TEXT,
    deadline DATE,
    employer_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employer_id) REFERENCES employers(employer_id) ON DELETE SET NULL
);

-- Applications table
CREATE TABLE IF NOT EXISTS applications (
    application_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    opportunity_type VARCHAR(20) NOT NULL,
    opportunity_id INT NOT NULL,
    application_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);

-- Resources table
CREATE TABLE IF NOT EXISTS resources (
    resource_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    file_path VARCHAR(255),
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seed users are intentionally disabled in the schema. Create them via the application after setting a secure password.
-- Example: INSERT INTO users (username, password, role) VALUES ('admin', '<bcrypt-hash>', 'ADMIN');

INSERT IGNORE INTO employers (user_id, company_name, contact_person, email, phone, address)
VALUES (2, 'TechCorp Solutions', 'John Smith', 'hr@techcorp.com', '+1234567890', '123 Tech Street, Silicon Valley');

-- Sample jobs
INSERT IGNORE INTO jobs (title, description, requirements, deadline, employer_id) VALUES
('Software Engineer Intern', 'Join our engineering team for a 3-month internship building scalable web applications.', 'Java or Python, Git, Problem solving skills', DATE_ADD(CURDATE(), INTERVAL 30 DAY), 1),
('Frontend Developer', 'Full-time position for a passionate frontend developer to build amazing UIs.', 'React, JavaScript, CSS, HTML5, 2+ years experience', DATE_ADD(CURDATE(), INTERVAL 60 DAY), 1);

-- Sample internship
INSERT IGNORE INTO internships (title, description, requirements, deadline, employer_id) VALUES
('Data Science Internship', '6-month data science internship analyzing real business data.', 'Python, SQL, Statistics, Machine Learning basics', DATE_ADD(CURDATE(), INTERVAL 45 DAY), 1);

-- Sample resources
INSERT IGNORE INTO resources (title, description, file_path) VALUES
('CV Writing Guide', 'Comprehensive guide to writing a professional CV for university graduates.', '/resources/cv_writing_guide.pdf'),
('Interview Tips', 'Essential tips and techniques for acing your job interviews.', '/resources/interview_tips.pdf'),
('Career Development Roadmap', 'Step-by-step career development guide for technology professionals.', '/resources/career_roadmap.pdf');
