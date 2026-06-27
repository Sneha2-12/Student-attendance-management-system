# Student Attendance & Leave Management System

A role-based, academic attendance and leave tracking system designed with **Spring Boot** and **Thymeleaf MVC**, featuring an interactive exam calendar and secure JWT authentication.

This project is structured as a 3rd/4th-year B.E. Computer Science & Engineering major project, demonstrating advanced concepts in secure web application development, role-based access control (RBAC), database automation, and RESTful APIs.

---

## Key Features

1. **JWT-over-Cookie Authentication**:
   - Secure JSON Web Tokens (JWT) are generated on sign-in and stored in HttpOnly, SameSite=Strict cookies to securely authorize Thymeleaf MVC views and REST API endpoints.
   - Separate Sign-In and Sign-Up flows for all three roles (**Admin**, **Teacher**, **Student**).
2. **Role-Based Access Control (RBAC)**:
   - **Admin**: Oversees system metrics, manages student/teacher profiles, schedules examinations, and reviews leave applications.
   - **Teacher**: Marks daily student attendance, applies filters by class section, and manages leave approvals.
   - **Student**: Monitors overall and subject-wise attendance metrics, submits Duty and Medical leave requests, and views upcoming exams.
3. **Medical & Duty Leaves Automation**:
   - Students can apply for Medical Leave (ML) or Duty Leave (DL) with supporting documents.
   - Upon approval by a teacher or admin, the system **automatically overrides and populates** the student's attendance records for those dates.
   - Computes both **Academic Attendance** (raw present ratio) and **Effective Attendance** (adjusted present ratio including approved leaves).
4. **Interactive Exam Calendar**:
   - A visual, interactive calendar built with pure CSS and JavaScript displaying scheduled examinations.
   - Administrators and teachers can click directly on a date to schedule a new exam. Clicking on scheduled exam badges displays comprehensive details.

---

## Technology Stack

- **Backend**: Java 17/23, Spring Boot 3.2.4
- **Security**: Spring Security 6, JJWT (JSON Web Token) 0.11.5
- **Database**: H2 In-Memory/File Database (Zero-Setup)
- **Persistence**: Spring Data JPA, Hibernate
- **Frontend**: Thymeleaf Template Engine, Vanilla CSS, Vanilla JavaScript (AJAX/Fetch APIs)
- **Build Tool**: Maven

---

## Project Directory Structure

```
student-attendance-system/
├── src/
│   ├── main/
│   │   ├── java/com/attendance/system/
│   │   │   ├── config/          # Spring Security, JWT Filter, Database Seeder
│   │   │   ├── controller/      # Auth, Page View, and REST Controllers
│   │   │   ├── model/           # JPA Entities (User, Attendance, Leave, Exam, etc.)
│   │   │   ├── repository/      # JPA Repositories
│   │   │   └── service/         # Business Logic (Leave, Attendance, UserService)
│   │   └── resources/
│   │       ├── templates/       # Thymeleaf HTML Views (Dashboards, Calendar, Auth)
│   │       ├── static/          # Static Assets (CSS Stylesheets, JS Helpers)
│   │       └── application.properties # Server, H2 DB, and JWT Settings
├── GITHUB_INSTRUCTIONS.md       # Step-by-step instructions to upload to GitHub
├── pom.xml                      # Maven dependencies
└── README.md                    # Project documentation
```

---

## How to Set Up and Run

### 1. Prerequisites
- **Java JDK 17 or higher** installed.
- **Apache Maven** installed.

### 2. Run the Application
Open a terminal in the project root directory and execute:
```bash
mvn spring-boot:run
```

The application will start, and you can access it in your browser at:
**[http://localhost:8080](http://localhost:8080)**

### 3. Demo Credentials
The application is pre-seeded on startup with realistic mock data, including teachers, students, course schedules, exams, and attendance history:

- **Admin Account**:
  - Email: `admin@school.com`
  - Password: `admin123`
- **Teacher Account**:
  - Email: `teacher1@school.com`
  - Password: `teacher123`
- **Student Account**:
  - Email: `student1@school.com`
  - Password: `student123`

---

## Database Management
The system uses an in-memory H2 database. To access the database console:
1. Navigate to: **[http://localhost:8080/h2-console](http://localhost:8080/h2-console)**
2. Set **JDBC URL** to: `jdbc:h2:mem:attendancedb`
3. Set **Username** to: `sa`
4. Set **Password** to: `password`
5. Click **Connect**.
