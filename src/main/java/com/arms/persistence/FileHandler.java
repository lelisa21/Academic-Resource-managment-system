package com.arms.persistence;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.arms.domain.Assignment;
import com.arms.domain.Course;
import com.arms.domain.Enrollment;
import com.arms.domain.Grade;
import com.arms.domain.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class FileHandler {
    private static FileHandler instance;
    private final ObjectMapper objectMapper;
    private final String dataDirectory = "data/";

    private FileHandler() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT, true);
        objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public static synchronized FileHandler getInstance() {
        if (instance == null) {
            instance = new FileHandler();
        }
        return instance;
    }

    // User operations
    public void saveUser(User user) throws IOException {
        String fileName = dataDirectory + "users/" + user.getId() + ".json";
        objectMapper.writeValue(new File(fileName), user);
    }

    public User loadUser(String userId) throws IOException {
        String fileName = dataDirectory + "users/" + userId + ".json";
        return objectMapper.readValue(new File(fileName), User.class);
    }

    public List<User> loadAllUsers() throws IOException {
        List<User> users = new ArrayList<>();
        Path usersDir = Paths.get(dataDirectory + "users/");
        
        if (Files.exists(usersDir)) {
            List<Path> files = Files.list(usersDir)
                    .filter(path -> path.toString().endsWith(".json"))
                    .collect(Collectors.toList());
            
            for (Path file : files) {
                users.add(objectMapper.readValue(file.toFile(), User.class));
            }
        }
        
        return users;
    }

    public void deleteUser(String userId) throws IOException {
        String fileName = dataDirectory + "users/" + userId + ".json";
        Files.deleteIfExists(Paths.get(fileName));
    }

    // Course operations
    public void saveCourse(Course course) throws IOException {
        String fileName = dataDirectory + "courses/" + course.getId() + ".json";
        objectMapper.writeValue(new File(fileName), course);
    }

    public Course loadCourse(String courseId) throws IOException {
        String fileName = dataDirectory + "courses/" + courseId + ".json";
        return objectMapper.readValue(new File(fileName), Course.class);
    }

    public List<Course> loadAllCourses() throws IOException {
        List<Course> courses = new ArrayList<>();
        Path coursesDir = Paths.get(dataDirectory + "courses/");
        
        if (Files.exists(coursesDir)) {
            List<Path> files = Files.list(coursesDir)
                    .filter(path -> path.toString().endsWith(".json"))
                    .collect(Collectors.toList());
            
            for (Path file : files) {
                courses.add(objectMapper.readValue(file.toFile(), Course.class));
            }
        }
        
        return courses;
    }

    public void deleteCourse(String courseId) throws IOException {
        String fileName = dataDirectory + "courses/" + courseId + ".json";
        Files.deleteIfExists(Paths.get(fileName));
    }

    // Assignment operations
    public void saveAssignment(Assignment assignment) throws IOException {
        String fileName = dataDirectory + "assignments/" + assignment.getId() + ".json";
        objectMapper.writeValue(new File(fileName), assignment);
    }

    public Assignment loadAssignment(String assignmentId) throws IOException {
        String fileName = dataDirectory + "assignments/" + assignmentId + ".json";
        return objectMapper.readValue(new File(fileName), Assignment.class);
    }

    public List<Assignment> loadAllAssignments() throws IOException {
        List<Assignment> assignments = new ArrayList<>();
        Path assignmentsDir = Paths.get(dataDirectory + "assignments/");
        
        if (Files.exists(assignmentsDir)) {
            List<Path> files = Files.list(assignmentsDir)
                    .filter(path -> path.toString().endsWith(".json"))
                    .collect(Collectors.toList());
            
            for (Path file : files) {
                assignments.add(objectMapper.readValue(file.toFile(), Assignment.class));
            }
        }
        
        return assignments;
    }

    public void deleteAssignment(String assignmentId) throws IOException {
        String fileName = dataDirectory + "assignments/" + assignmentId + ".json";
        Files.deleteIfExists(Paths.get(fileName));
    }

    // Grade operations
    public void saveGrade(Grade grade) throws IOException {
        String fileName = dataDirectory + "grades/" + grade.getId() + ".json";
        objectMapper.writeValue(new File(fileName), grade);
    }

    public Grade loadGrade(String gradeId) throws IOException {
        String fileName = dataDirectory + "grades/" + gradeId + ".json";
        return objectMapper.readValue(new File(fileName), Grade.class);
    }

    public List<Grade> loadAllGrades() throws IOException {
        List<Grade> grades = new ArrayList<>();
        Path gradesDir = Paths.get(dataDirectory + "grades/");
        
        if (Files.exists(gradesDir)) {
            List<Path> files = Files.list(gradesDir)
                    .filter(path -> path.toString().endsWith(".json"))
                    .collect(Collectors.toList());
            
            for (Path file : files) {
                grades.add(objectMapper.readValue(file.toFile(), Grade.class));
            }
        }
        
        return grades;
    }

    public void deleteGrade(String gradeId) throws IOException {
        String fileName = dataDirectory + "grades/" + gradeId + ".json";
        Files.deleteIfExists(Paths.get(fileName));
    }

    // Enrollment operations
    public void saveEnrollment(Enrollment enrollment) throws IOException {
        String fileName = dataDirectory + "enrollments/" + enrollment.getId() + ".json";
        objectMapper.writeValue(new File(fileName), enrollment);
    }

    public Enrollment loadEnrollment(String enrollmentId) throws IOException {
        String fileName = dataDirectory + "enrollments/" + enrollmentId + ".json";
        return objectMapper.readValue(new File(fileName), Enrollment.class);
    }

    public List<Enrollment> loadAllEnrollments() throws IOException {
        List<Enrollment> enrollments = new ArrayList<>();
        Path enrollmentsDir = Paths.get(dataDirectory + "enrollments/");
        
        if (Files.exists(enrollmentsDir)) {
            List<Path> files = Files.list(enrollmentsDir)
                    .filter(path -> path.toString().endsWith(".json"))
                    .collect(Collectors.toList());
            
            for (Path file : files) {
                enrollments.add(objectMapper.readValue(file.toFile(), Enrollment.class));
            }
        }
        
        return enrollments;
    }

    public void deleteEnrollment(String enrollmentId) throws IOException {
        String fileName = dataDirectory + "enrollments/" + enrollmentId + ".json";
        Files.deleteIfExists(Paths.get(fileName));
    }

    // Backup operations
    public void backupData() throws IOException {
        String timestamp = String.valueOf(System.currentTimeMillis());
        Path backupDir = Paths.get(dataDirectory + "backups/" + timestamp);
        Files.createDirectories(backupDir);

        // Backup all data
        backupDirectory("users", backupDir);
        backupDirectory("courses", backupDir);
        backupDirectory("assignments", backupDir);
        backupDirectory("grades", backupDir);
        backupDirectory("enrollments", backupDir);
    }

    private void backupDirectory(String dirName, Path backupDir) throws IOException {
        Path sourceDir = Paths.get(dataDirectory + dirName);
        if (Files.exists(sourceDir)) {
            Path targetDir = backupDir.resolve(dirName);
            Files.createDirectories(targetDir);
            
            Files.list(sourceDir)
                    .filter(Files::isRegularFile)
                    .forEach(source -> {
                        try {
                            Files.copy(source, targetDir.resolve(source.getFileName()));
                        } catch (IOException e) {
                            System.err.println("Failed to backup: " + source);
                        }
                    });
        }
    }
}
