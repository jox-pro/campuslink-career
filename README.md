# CampusLink Career

A professional Java desktop application for university career management, connecting students, employers, and administrators in one centralized platform.

## Features

### For Students
- Register and manage profile
- Browse job and internship listings
- Apply for opportunities
- Track application status
- Access career resources (CV guides, interview tips)

### For Employers
- Post job and internship opportunities
- Review applicants
- Update application status (Pending, Reviewed, Shortlisted, Accepted, Rejected)

### For Administrators
- Manage all students, employers, and listings
- Generate and export reports
- Manage career resources

## Tech Stack

| Technology | Purpose |
|---|---|
| Java 17 | Core language |
| JavaFX 17 | Desktop GUI framework |
| Maven | Build and dependency management |
| MySQL 8 | Relational database |
| JDBC | Database connectivity |
| BCrypt (jBCrypt) | Password hashing |

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.0+

### Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/jox-pro/campuslink-career
   cd campuslink-career
   ```

2. Set up the database:
   ```bash
   mysql -u root -p < database/schema.sql
   ```

3. Configure database connection in `src/main/resources/db.properties`:
   ```properties
   db.url=jdbc:mysql://localhost:3306/campuslink_career?useSSL=false&serverTimezone=UTC
   db.username=root
   db.password=yourpassword
   ```

4. Run the application:
   ```bash
   mvn javafx:run
   ```

### Default Credentials

| Role | Username | Password |
|---|---|---|
| Admin | admin | admin123 |
| Employer | techcorp | employer123 |

## Project Structure

```
src/main/java/com/campuslink/
├── app/            Application entry point (Main.java)
├── controllers/    JavaFX UI controllers
├── dao/            Database access objects
├── models/         Data model POJOs
├── services/       Business logic layer
└── utils/          Database, crypto, validation utilities

src/main/resources/
├── fxml/           FXML view files
├── css/            Stylesheets
└── images/         Application icons

database/
└── schema.sql      MySQL setup script
```

## License

MIT License — University final-year project
