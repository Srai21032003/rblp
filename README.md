# Role-Based Learning Platform (RBLP)

A reactive, role-based backend system built with **Vert.x**, **RxJava 3**, **Ebean ORM**, and **MySQL** for managing users, KYC verification, and bulk onboarding operations.

---

## 📋 Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Setup Instructions](#setup-instructions)
- [Configuration](#configuration)
- [API Endpoints](#api-endpoints)
- [Database Schema](#database-schema)
- [Running the Application](#running-the-application)
- [Testing with Postman](#testing-with-postman)

---

## ✨ Features

### 🔐 Authentication & Authorization
- JWT-based authentication
- Role-based access control (ADMIN, TEACHER, STUDENT)
- Secure password hashing with BCrypt

### 👥 User Management
- User registration and login
- Admin-controlled user onboarding
- Profile management for Teachers and Students
- User activation/deactivation

### 📄 KYC Verification System
- Document upload and validation (PAN, AADHAAR, PASSPORT)
- Automated document validation with regex patterns
- File type and size validation
- Admin approval/rejection workflow
- Real-time KYC status tracking

### 📊 Bulk Upload
- CSV-based bulk user onboarding
- Asynchronous processing with RxJava
- Detailed error reporting per record
- Upload status tracking

---

## 🛠 Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 21 | Programming Language |
| **Vert.x** | 5.0.6 | Reactive Web Framework |
| **RxJava** | 3.1.12 | Reactive Programming |
| **Ebean ORM** | 14.6.0 | Database ORM |
| **MySQL** | 8.x | Database |
| **JJWT** | 0.12.3 | JWT Token Management |
| **BCrypt** | 0.4 | Password Hashing |
| **Lombok** | 1.18.38 | Boilerplate Reduction |
| **Gradle** | 9.2.0 | Build Tool |

---

## 📁 Project Structure

```
rblp/
├── src/main/
│   ├── java/com/internship/rblp/
│   │   ├── MainVerticle.java              # Application entry point
│   │   ├── config/
│   │   │   └── AppDatabaseConfig.java     # Database configuration
│   │   ├── models/
│   │   │   ├── entities/                  # JPA entities
│   │   │   │   ├── User.java
│   │   │   │   ├── StudentProfile.java
│   │   │   │   ├── TeacherProfile.java
│   │   │   │   ├── KycDetails.java
│   │   │   │   ├── KycDocument.java
│   │   │   │   ├── BulkUpload.java
│   │   │   │   └── BulkUploadError.java
│   │   │   └── enums/                     # Enumerations
│   │   │       ├── Role.java
│   │   │       ├── KycStatus.java
│   │   │       ├── DocType.java
│   │   │       ├── ValidationStatus.java
│   │   │       └── BulkStatus.java
│   │   ├── repository/                    # Data access layer
│   │   │   ├── UserRepository.java
│   │   │   ├── KycRepository.java
│   │   │   └── BulkUploadRepository.java
│   │   ├── service/                       # Business logic
│   │   │   ├── AuthService.java
│   │   │   ├── AdminService.java
│   │   │   ├── StudentService.java
│   │   │   ├── TeacherService.java
│   │   │   ├── KycService.java
│   │   │   └── BulkUploadService.java
│   │   ├── handlers/                      # HTTP request handlers
│   │   │   ├── auth/
│   │   │   ├── admin/
│   │   │   ├── student/
│   │   │   ├── teacher/
│   │   │   └── kyc/
│   │   ├── routers/                       # Route definitions
│   │   │   ├── AuthRouter.java
│   │   │   ├── AdminRouter.java
│   │   │   ├── StudentRouter.java
│   │   │   ├── TeacherRouter.java
│   │   │   └── UserRouter.java
│   │   └── util/                          # Utility classes
│   │       ├── JwtUtil.java
│   │       └── KycValidationUtil.java
│   └── resources/
│       └── application.properties         # Application configuration
├── build.gradle                           # Gradle build configuration
├── settings.gradle                        # Gradle settings
└── README.md                              # This file
```

---

## 📦 Prerequisites

- **JDK 21** or higher
- **MySQL 8.x** or higher
- **Gradle 9.x** (or use included wrapper)
- **Git** (for version control)

---

## 🚀 Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd rblp
```

### 2. Create MySQL Database

```sql
CREATE DATABASE rblp_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Configure Database Connection

Edit `src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=8080

# Database Configuration
datasource.username=root
datasource.password=YOUR_PASSWORD
datasource.url=jdbc:mysql://localhost:3306/rblp_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
datasource.driver=com.mysql.cj.jdbc.Driver

# Ebean Configuration
ebean.ddl.generate=true
ebean.ddl.run=true
ebean.default.datasource=db

# JWT Security
jwt.secret=MySuperSecretKeyForInternshipProject2026_MustBeLongEnough!
jwt.issuer=rblp-backend
jwt.expiration=86400
```

### 4. Build the Project

```bash
./gradlew clean build
```

---

## ⚙️ Configuration

### Database Auto-Generation

The application uses Ebean's DDL generation:
- `ebean.ddl.generate=true` - Generates SQL schema
- `ebean.ddl.run=true` - Automatically creates tables on startup

**⚠️ Warning:** Set `ebean.ddl.run=false` in production!

### JWT Configuration

- **Secret Key**: Minimum 256 bits for HMAC-SHA256
- **Expiration**: 86400 seconds (24 hours)
- **Claims**: userId, role, email

---

## 🌐 API Endpoints

### 🔓 Public Endpoints

#### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | User login |
| POST | `/api/auth/register` | User registration |

**Login Request:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Register Request:**
```json
{
  "fullName": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "role": "STUDENT"
}
```

---

### 🔒 Protected Endpoints (Requires JWT)

**Header Required:**
```
Authorization: Bearer <jwt-token>
```

#### Admin Routes (`/api/admin/*`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/admin/onboard/teacher` | Onboard new teacher |
| POST | `/api/admin/onboard/student` | Onboard new student |
| GET | `/api/admin/users` | Get all users |
| PUT | `/api/admin/users/:userId/toggle` | Activate/deactivate user |
| PUT | `/api/admin/profile` | Update admin profile |
| POST | `/api/admin/bulk-upload` | Upload CSV for bulk onboarding |
| GET | `/api/admin/bulk-upload/:uploadId/status` | Get upload status |
| GET | `/api/admin/bulk-upload/:uploadId/errors` | Get upload errors |

**Bulk Upload CSV Format:**
```csv
fullName,email,mobileNumber,role,password
John Doe,john@example.com,9876543210,STUDENT,Welcome123
Jane Smith,jane@example.com,9876543211,TEACHER,Welcome123
```

#### Student Routes (`/api/student/*`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| PUT | `/api/student/profile` | Update student profile |
| POST | `/api/student/kyc/submit` | Submit KYC documents |
| GET | `/api/student/kyc/status` | Get KYC status |

**KYC Submission Request:**
```json
{
  "address": "123 Main St, City",
  "dob": "1995-05-15",
  "documents": [
    {
      "docType": "PAN",
      "documentNumber": "ABCDE1234F",
      "filePath": "/uploads/pan.pdf",
      "originalFileName": "pan.pdf",
      "nameOnDoc": "John Doe"
    },
    {
      "docType": "AADHAAR",
      "documentNumber": "234567890123",
      "filePath": "/uploads/aadhaar.pdf",
      "originalFileName": "aadhaar.pdf",
      "nameOnDoc": "John Doe"
    },
    {
      "docType": "PASSPORT",
      "documentNumber": "A1234567",
      "filePath": "/uploads/passport.pdf",
      "originalFileName": "passport.pdf",
      "nameOnDoc": "John Doe"
    }
  ]
}
```

#### Teacher Routes (`/api/teacher/*`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| PUT | `/api/teacher/profile` | Update teacher profile |
| POST | `/api/teacher/kyc/submit` | Submit KYC documents |
| GET | `/api/teacher/kyc/status` | Get KYC status |

#### KYC Management (`/api/admin/kyc/*`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/kyc/all` | Get all KYC submissions |
| GET | `/api/admin/kyc/:kycId` | Get KYC details |
| PUT | `/api/admin/kyc/:kycId/approve` | Approve KYC |
| PUT | `/api/admin/kyc/:kycId/reject` | Reject KYC |

---

## 🗄️ Database Schema

### Users Table
```sql
CREATE TABLE users (
  user_id UUID PRIMARY KEY,
  role VARCHAR(20) NOT NULL,
  full_name VARCHAR(255) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  mobile_number VARCHAR(20),
  password VARCHAR(255) NOT NULL,
  is_active BOOLEAN DEFAULT TRUE,
  deleted BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);
```

### Student/Teacher Profiles
```sql
CREATE TABLE student_profiles (
  user_id UUID PRIMARY KEY,
  course_enrolled VARCHAR(255),
  FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE teacher_profiles (
  user_id UUID PRIMARY KEY,
  qualification VARCHAR(255),
  experience_years INT,
  FOREIGN KEY (user_id) REFERENCES users(user_id)
);
```

### KYC Tables
```sql
CREATE TABLE kyc_details (
  id UUID PRIMARY KEY,
  user_id UUID UNIQUE NOT NULL,
  address TEXT,
  dob DATE,
  status VARCHAR(20) DEFAULT 'PENDING',
  admin_remarks TEXT,
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE kyc_documents (
  id UUID PRIMARY KEY,
  kyc_details_id UUID NOT NULL,
  doc_type VARCHAR(20) NOT NULL,
  document_number VARCHAR(50),
  file_path VARCHAR(500) NOT NULL,
  validation_status VARCHAR(20) DEFAULT 'MANUAL_REVIEW',
  validation_message TEXT,
  FOREIGN KEY (kyc_details_id) REFERENCES kyc_details(id)
);
```

---

## 🏃 Running the Application

### Development Mode

```bash
./gradlew run
```

### Build Fat JAR

```bash
./gradlew shadowJar
```

The fat JAR will be created at: `build/libs/rblp-1.0.0-SNAPSHOT-fat.jar`

### Run Fat JAR

```bash
java -jar build/libs/rblp-1.0.0-SNAPSHOT-fat.jar
```

### Health Check

```bash
curl http://localhost:8080/health
```

**Response:**
```json
{
  "status": "UP"
}
```

---

## 📮 Testing with Postman

1. Import the Postman collection from `postman/RBLP_Postman_Collection.json`
2. Set environment variables:
   - `base_url`: `http://localhost:8080`
   - `jwt_token`: (obtained after login)

### Test Flow

1. **Register/Login** → Get JWT token
2. **Set Authorization Header** → `Bearer <token>`
3. **Test Role-Specific Endpoints**
4. **Submit KYC** (Student/Teacher)
5. **Admin Reviews KYC**
6. **Bulk Upload** (Admin only)

---

## 🔒 Security Features

- **Password Hashing**: BCrypt with salt
- **JWT Authentication**: Stateless token-based auth
- **Role-Based Access Control**: Endpoint protection by role
- **Input Validation**: Document format and size validation
- **SQL Injection Prevention**: Parameterized queries via Ebean ORM

---

## 📝 Document Validation Rules

### PAN Card
- Format: `ABCDE1234F`
- Pattern: 5 letters + 4 digits + 1 letter

### Aadhaar Card
- Format: `234567890123`
- Pattern: 12 digits (first digit 2-9)

### Passport
- Format: `A1234567`
- Pattern: 1 letter + 7 digits

### File Constraints
- **Allowed Types**: PDF, JPG, PNG
- **Max Size**: 5 MB

---

## 🐛 Troubleshooting

### Database Connection Issues
```
Error: Access denied for user 'root'@'localhost'
```
**Solution**: Check MySQL credentials in `application.properties`

### Port Already in Use
```
Error: Address already in use
```
**Solution**: Change `server.port` in `application.properties` or kill process on port 8080

### JWT Token Expired
```
Error: 401 Unauthorized
```
**Solution**: Login again to get a new token

---

## 📄 License

This project is developed as part of an internship assignment.

---

## 👥 Contributors

- **Developer**: **Shivank Rai**
- **Organization**: Dice Enterprises Ltd.

---

## 📞 Support

For issues or questions, please contact: srai21032003@gmail.com
