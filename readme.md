# Academic Records Management System (ARMS)

A comprehensive JavaFX desktop application for managing academic records, built with modern Java technologies.

##  Features

###  Student Dashboard
- **Course Management**: Browse and enroll in available courses
- **Assignment Submission**: Submit assignments with file attachments
- **Grade Tracking**: View grades, calculate GPA, generate transcripts
- **Schedule View**: Visual weekly class schedule
- **Transcript Generation**: Create official academic transcripts

###  Teacher Dashboard
- **Course Management**: Create and manage courses
- **Assignment Creation**: Design and publish assignments
- **Grading System**: Grade student submissions with feedback
- **Analytics**: View class performance statistics
- **Student Management**: Track enrolled students

###  Admin Dashboard
- **User Management**: Add, edit, and manage users (Students, Teachers, Admins)
- **Course Administration**: Create and manage all courses
- **System Analytics**: View user statistics and system health
- **Data Backup**: Automatic backup and data management

##  Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.8+
- Windows 10/11

### Installation for Windows

1. **Clone or download the project**
   - Extract the project to a folder (e.g., `C:\ARMS-GUI`)

2. **Open Command Prompt as Administrator**
```cmd
cd C:\ARMS-GUI
```

3. **Build the project**
```cmd
mvn clean compile
```

4. **Run the application**
```cmd
mvn javafx:run
```

### Run with Sample Data (Windows)
```cmd
del /Q data\*.*
mvn clean compile
mvn javafx:run
```

##  Default Login Credentials

| Role | Username | Password |
|------|----------|----------|
| Admin | `admin` | `admin123` |
| Teacher | `amir` | `password` |
| Student | `sarah` | `password` |

##  Project Structure

```
ARMS-GUI/
├── src/main/java/com/arms/
│   ├── domain/           # Domain models (User, Course, Assignment, etc.)
│   ├── service/          # Business logic services
│   ├── persistence/      # Data persistence layer
│   ├── gui/             # JavaFX controllers and views
│   ├── util/            # Utility classes
│   └── config/          # Configuration classes
├── src/main/resources/   # FXML views, CSS, images
├── data/                # Application data storage (auto-created)
├── logs/                # Application logs (auto-created)
└── README.md           # This file
```

##  Technical Stack

- **Language**: Java 17
- **Framework**: JavaFX 17
- **Build Tool**: Maven
- **Dependency Management**: Maven Central
- **Data Persistence**: JSON files with Jackson
- **Password Hashing**: BCrypt
- **UI Components**: ControlsFX
- **Charts**: JavaFX Charts

##  Key Features

### Data Management
- JSON-based file storage
- Automatic backups every 5 minutes
- Data integrity validation
- Concurrent access support

### Security
- BCrypt password hashing
- Role-based access control
- Session management
- Input validation

### User Experience
- Responsive UI with CSS styling
- Real-time updates
- Intuitive navigation
- Help system

##  Features by Role

### Student Features
- [x] Course enrollment
- [x] Assignment submission
- [x] Grade viewing
- [x] Transcript generation
- [x] Profile management
- [x] Schedule viewing

### Teacher Features
- [x] Course creation
- [x] Assignment management
- [x] Student grading
- [x] Performance analytics
- [x] Grade publishing

### Admin Features
- [x] User management
- [x] Course administration
- [x] System monitoring
- [x] Data backup/restore
- [x] Permission management

##  Testing

Run unit tests:
```cmd
mvn test
```

## Build Options (Windows)

### Create executable JAR
```cmd
mvn clean package
```

### Run without Maven
```cmd
java -jar target\demo-1.0-SNAPSHOT-jar-with-dependencies.jar
```

##  Data Backup & Recovery

- **Automatic**: Every 5 minutes
- **Manual**: Admin dashboard → Backup Data
- **Location**: `data\backups\` directory
- **Format**: Timestamped JSON files

##  Future Enhancements

- [ ] Email notifications
- [ ] Calendar integration
- [ ] Mobile companion app
- [ ] Advanced reporting
- [ ] Attendance tracking
- [ ] Discussion forums
- [ ] File versioning for submissions



---

##  Educational Purpose Notice

This Academic Records Management System (ARMS) is developed as an **educational project** to demonstrate:

1. **Java Desktop Application Development** using JavaFX
2. **Object-Oriented Programming** principles and design patterns
3. **Data Persistence** with JSON file storage
4. **Multi-tier Architecture** (Presentation, Business, Data layers)
5. **Role-Based Access Control** implementation
6. **Modern Java Features** including streams, lambdas, and modules

### Learning Objectives:
- Building complete CRUD operations
- Implementing authentication and authorization
- Creating responsive GUI applications
- Working with file I/O and serialization
- Developing multi-user systems with proper data separation
- Following clean code practices and MVC pattern

This project serves as a **comprehensive example** for students learning:
- Software Engineering principles
- Desktop application development
- Database design (even with file storage)
- User interface design
- System architecture

**Note**: This system is for educational demonstration and should not be used in production environments without proper security audits and database migration.

---


