package com.arms.persistence;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.mindrot.jbcrypt.BCrypt;

import com.arms.domain.Admin;
import com.arms.domain.Assignment;
import com.arms.domain.Course;
import com.arms.domain.Enrollment;
import com.arms.domain.Grade;
import com.arms.domain.Student;
import com.arms.domain.Teacher;
import com.arms.domain.User;
import com.arms.domain.enums.UserRole;
import com.arms.domain.enums.UserStatus;
import com.arms.util.Logger;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class DataManager {

    private static DataManager instance;

    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, Course> courses = new ConcurrentHashMap<>();
    private final Map<String, Assignment> assignments = new ConcurrentHashMap<>();
    private final Map<String, Grade> grades = new ConcurrentHashMap<>();
    private final Map<String, Enrollment> enrollments = new ConcurrentHashMap<>();

    public Map<String, User> getUsers() {
        return users;
    }

    public Map<String, Course> getCourses() {
        return courses;
    }

    public Map<String, Assignment> getAssignments() {
        return assignments;
    }

    public Map<String, Grade> getGrades() {
        return grades;
    }

    public Map<String, Enrollment> getEnrollments() {
        return enrollments;
    }

    private final ObjectMapper objectMapper;
    private final String dataDirectory = "data/";
    private final ScheduledExecutorService autoSaveScheduler;
    private volatile boolean isDirty = false;

    private DataManager() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        // Ignore unknown properties from older/alternate JSON shapes
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        createDataDirectory();
        loadAllData();
        // Seed sample data if empty
        seedSampleDataIfEmpty();

        // Auto-save every 5 minutes
        autoSaveScheduler = Executors.newSingleThreadScheduledExecutor();
        autoSaveScheduler.scheduleAtFixedRate(this::autoSave, 5, 5, TimeUnit.MINUTES);

        // Shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    public static synchronized DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    private void createDataDirectory() {
        try {
            Files.createDirectories(Paths.get(dataDirectory));
            Files.createDirectories(Paths.get(dataDirectory + "users/"));
            Files.createDirectories(Paths.get(dataDirectory + "courses/"));
            Files.createDirectories(Paths.get(dataDirectory + "assignments/"));
            Files.createDirectories(Paths.get(dataDirectory + "grades/"));
            Files.createDirectories(Paths.get(dataDirectory + "enrollments/"));
            Files.createDirectories(Paths.get(dataDirectory + "backups/"));
        } catch (IOException e) {
            Logger.error("Failed to create data directories", e);
        }
    }

    private void loadAllData() {
        loadUsers();
        loadCourses();
        loadAssignments();
        loadGrades();
        loadEnrollments();
        Logger.info("Data loaded successfully");
    }

    private void loadUsers() {
        try {
            Path usersDir = Paths.get(dataDirectory + "users/");
            if (Files.exists(usersDir)) {
                Files.list(usersDir)
                        .filter(path -> path.toString().endsWith(".json"))
                        .forEach(this::loadUser);
            }
        } catch (IOException e) {
            Logger.error("Failed to load users", e);
        }
    }

    private void loadUser(Path filePath) {
        try {
            User user = objectMapper.readValue(filePath.toFile(), User.class);
            users.put(user.getId(), user);
        } catch (IOException e) {
            Logger.error("Failed to load user from: " + filePath, e);
        }
    }

    private void loadCourses() {
        try {
            List<Course> loaded = FileHandler.getInstance().loadAllCourses();
            for (Course c : loaded) {
                courses.put(c.getId(), c);
            }
        } catch (IOException e) {
            Logger.error("Failed to load courses", e);
        }
    }

    private void loadAssignments() {
        try {
            List<Assignment> loaded = FileHandler.getInstance().loadAllAssignments();
            for (Assignment a : loaded) {
                assignments.put(a.getId(), a);
            }
        } catch (IOException e) {
            Logger.error("Failed to load assignments", e);
        }
    }

    private void loadGrades() {
        try {
            List<Grade> loaded = FileHandler.getInstance().loadAllGrades();
            for (Grade g : loaded) {
                grades.put(g.getId(), g);
            }
        } catch (IOException e) {
            Logger.error("Failed to load grades", e);
        }
    }

    private void loadEnrollments() {
        try {
            List<Enrollment> loaded = FileHandler.getInstance().loadAllEnrollments();
            for (Enrollment en : loaded) {
                enrollments.put(en.getId(), en);
            }
        } catch (IOException e) {
            Logger.error("Failed to load enrollments", e);
        }
    }

    // Similar load methods for courses, assignments, grades, enrollments...
    public void saveUser(User user) {
        try {
            String fileName = dataDirectory + "users/" + user.getId() + ".json";
            objectMapper.writeValue(new File(fileName), user);
            users.put(user.getId(), user);
            markDirty();
            Logger.debug("User saved: " + user.getUsername());
        } catch (IOException e) {
            Logger.error("Failed to save user: " + user.getUsername(), e);
        }
    }

    public void deleteUser(String userId) {
        try {
            String fileName = dataDirectory + "users/" + userId + ".json";
            Files.deleteIfExists(Paths.get(fileName));
            users.remove(userId);
            markDirty();
            Logger.info("User deleted: " + userId);
        } catch (IOException e) {
            Logger.error("Failed to delete user: " + userId, e);
        }
    }

    public void saveCourse(Course course) {
        try {
            FileHandler.getInstance().saveCourse(course);
            courses.put(course.getId(), course);
            markDirty();
            Logger.debug("Course saved: " + course.getCourseCode());
        } catch (IOException e) {
            Logger.error("Failed to save course: " + course.getCourseCode(), e);
        }
    }

    public void deleteCourse(String courseId) {
        try {
            FileHandler.getInstance().deleteCourse(courseId);
            courses.remove(courseId);
            markDirty();
            Logger.info("Course deleted: " + courseId);
        } catch (IOException e) {
            Logger.error("Failed to delete course: " + courseId, e);
        }
    }

    public void saveAssignment(Assignment assignment) {
        try {
            FileHandler.getInstance().saveAssignment(assignment);
            assignments.put(assignment.getId(), assignment);
            markDirty();
            Logger.debug("Assignment saved: " + assignment.getTitle());
        } catch (IOException e) {
            Logger.error("Failed to save assignment: " + assignment.getTitle(), e);
        }
    }

    public void deleteAssignment(String assignmentId) {
        try {
            FileHandler.getInstance().deleteAssignment(assignmentId);
            assignments.remove(assignmentId);
            markDirty();
            Logger.info("Assignment deleted: " + assignmentId);
        } catch (IOException e) {
            Logger.error("Failed to delete assignment: " + assignmentId, e);
        }
    }

    public void saveGrade(Grade grade) {
        try {
            FileHandler.getInstance().saveGrade(grade);
            grades.put(grade.getId(), grade);
            markDirty();
            Logger.debug("Grade saved: " + grade.getId());
        } catch (IOException e) {
            Logger.error("Failed to save grade: " + grade.getId(), e);
        }
    }

    public void deleteGrade(String gradeId) {
        try {
            FileHandler.getInstance().deleteGrade(gradeId);
            grades.remove(gradeId);
            markDirty();
            Logger.info("Grade deleted: " + gradeId);
        } catch (IOException e) {
            Logger.error("Failed to delete grade: " + gradeId, e);
        }
    }

    public void saveEnrollment(Enrollment enrollment) {
        try {
            FileHandler.getInstance().saveEnrollment(enrollment);
            enrollments.put(enrollment.getId(), enrollment);
            markDirty();
            Logger.debug("Enrollment saved: " + enrollment.getId());
        } catch (IOException e) {
            Logger.error("Failed to save enrollment: " + enrollment.getId(), e);
        }
    }

    public void deleteEnrollment(String enrollmentId) {
        try {
            FileHandler.getInstance().deleteEnrollment(enrollmentId);
            enrollments.remove(enrollmentId);
            markDirty();
            Logger.info("Enrollment deleted: " + enrollmentId);
        } catch (IOException e) {
            Logger.error("Failed to delete enrollment: " + enrollmentId, e);
        }
    }

    // Similar save/delete methods for other entities...
    public Optional<User> findUserByUsername(String username) {
        return users.values().stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    public List<Course> getCoursesByTeacher(String teacherId) {
        return courses.values().stream()
                .filter(course -> course.getTeacherId().equals(teacherId))
                .collect(Collectors.toList());
    }

    public List<Enrollment> getEnrollmentsByStudent(String studentId) {
        return enrollments.values().stream()
                .filter(enrollment -> enrollment.getStudentId().equals(studentId))
                .collect(Collectors.toList());
    }

    public List<Grade> getGradesByStudentAndCourse(String studentId, String courseId) {
        return grades.values().stream()
                .filter(grade -> grade.getStudentId().equals(studentId)
                && grade.getCourseId().equals(courseId))
                .collect(Collectors.toList());
    }

    private void markDirty() {
        isDirty = true;
    }

    private void autoSave() {
        if (isDirty) {
            createBackup();
            isDirty = false;
        }
    }

    private void createBackup() {
        try {
            String timestamp = String.valueOf(System.currentTimeMillis());
            Path backupDir = Paths.get(dataDirectory + "backups/" + timestamp);
            Files.createDirectories(backupDir);

            // Copy all data files to backup directory
            Files.walk(Paths.get(dataDirectory))
                    .filter(path -> !path.toString().contains("backups"))
                    .filter(Files::isRegularFile)
                    .forEach(source -> {
                        try {
                            Path relative = Paths.get(dataDirectory).relativize(source);
                            Path target = backupDir.resolve(relative);
                            Files.createDirectories(target.getParent());
                            Files.copy(source, target);
                        } catch (IOException e) {
                            Logger.error("Backup failed for: " + source, e);
                        }
                    });
        } catch (IOException e) {
            Logger.error("Failed to create backup", e);
        }
    }

    private void seedSampleDataIfEmpty() {
        try {
            if (users.isEmpty()) {
                // Create admin
                Admin admin = new Admin();
                admin.setId(IDGenerator.generateUserId());
                admin.setAdminId(IDGenerator.generateAdminId());
                admin.setUsername("admin");
                admin.setFirstName("System");
                admin.setLastName("Admin");
                admin.setEmail("admin@example.com");
                admin.setPasswordHash(BCrypt.hashpw("admin123", BCrypt.gensalt(12)));
                admin.setRole(UserRole.ADMIN);
                admin.setStatus(UserStatus.ACTIVE);
                admin.setCreatedAt(java.time.LocalDateTime.now());
                saveUser(admin);

                // Create a teacher
                Teacher teacher = new Teacher();
                teacher.setId(IDGenerator.generateUserId());
                teacher.setEmployeeId(IDGenerator.generateTeacherId());
                teacher.setUsername("amir");
                teacher.setFirstName("Amir");
                teacher.setLastName("Tola");
                
                teacher.setEmail("amir@example.com");
                teacher.setPasswordHash(BCrypt.hashpw("password", BCrypt.gensalt(12)));
                teacher.setRole(UserRole.TEACHER);
                teacher.setStatus(UserStatus.ACTIVE);
                saveUser(teacher);

                // Create a student
                Student student = new Student();
                student.setId(IDGenerator.generateUserId());
                student.setStudentId(IDGenerator.generateStudentId());
                student.setUsername("sarah");
                student.setFirstName("Sarah");
                student.setLastName("Lee");
                student.setEmail("sarah@example.com");
                student.setPasswordHash(BCrypt.hashpw("password", BCrypt.gensalt(12)));
                student.setRole(UserRole.STUDENT);
                student.setStatus(UserStatus.ACTIVE);
                saveUser(student);

                // Create a sample course
                Course course = new Course();
                course.setId(IDGenerator.generateCourseId());
                course.setCourseCode("CS101");
                course.setTitle("Introduction to Programming");
                course.setDescription("Basic programming concepts.");
                course.setCredits(3);
                course.setDepartment("Computer Science");
                course.setSemester("Fall");
                course.setTeacherId(teacher.getId());
                course.setMaxStudents(30);
                course.setCurrentEnrollment(1);
                course.setActive(true);
                saveCourse(course);

                // Enroll student
                Enrollment enrollment = new Enrollment();
                enrollment.setId(IDGenerator.generateEnrollmentId());
                enrollment.setStudentId(student.getId());
                enrollment.setCourseId(course.getId());
                enrollment.setEnrolledAt(java.time.LocalDateTime.now());
                enrollment.setStatus(com.arms.domain.enums.EnrollmentStatus.ENROLLED);
                saveEnrollment(enrollment);

                // Create assignment
                Assignment assignment = new Assignment();
                assignment.setId(IDGenerator.generateAssignmentId());
                assignment.setCourseId(course.getId());
                assignment.setTitle("Homework 1");
                assignment.setDescription("Solve exercises 1-5");
                assignment.setMaxScore(100);
                assignment.setWeight(10);
                assignment.setDueDate(java.time.LocalDateTime.now().plusDays(7));
                saveAssignment(assignment);

                // Create a sample grade
                Grade grade = new Grade();
                grade.setId(IDGenerator.generateGradeId());
                grade.setStudentId(student.getId());
                grade.setCourseId(course.getId());
                grade.setAssignmentId(assignment.getId());
                grade.setScore(85);
                grade.setMaxScore(100);
                grade.setGradedAt(java.time.LocalDateTime.now());
                grade.calculateLetterGrade();
                saveGrade(grade);

                Logger.info("Sample data created: admin/admin123, teacher/jdoe, student/sarah");
            }
        } catch (Exception e) {
            Logger.error("Failed to seed sample data", e);
        }
    }

    private void shutdown() {
        autoSaveScheduler.shutdown();
        try {
            if (!autoSaveScheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                autoSaveScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            autoSaveScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        if (isDirty) {
            createBackup();
        }
    }
}
