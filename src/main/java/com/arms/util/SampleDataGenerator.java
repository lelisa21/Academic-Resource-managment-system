package com.arms.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.arms.domain.Admin;
import com.arms.domain.Assignment;
import com.arms.domain.Course;
import com.arms.domain.Grade;
import com.arms.domain.Student;
import com.arms.domain.Teacher;
import com.arms.domain.enums.AssignmentType;
import com.arms.domain.enums.UserRole;
import com.arms.domain.enums.UserStatus;
import com.arms.persistence.DataManager;
import com.arms.persistence.IDGenerator;
import com.arms.service.UserService;

public class SampleDataGenerator {
    
    public static void generateSampleData() {
        DataManager dataManager = DataManager.getInstance();
        UserService userService = UserService.getInstance();
        
        // Clear existing data
        dataManager.getUsers().clear();
        dataManager.getCourses().clear();
        dataManager.getAssignments().clear();
        dataManager.getGrades().clear();
        
        // Generate Admin
        Admin admin = new Admin();
        admin.setId(IDGenerator.generateUserId());
        admin.setUsername("admin");
        admin.setPasswordHash(userService.hashPassword("admin123"));
        admin.setFirstName("System");
        admin.setLastName("Administrator");
        admin.setEmail("admin@university.edu");
        admin.setRole(UserRole.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setAdminId("ADM001");
        admin.setAccessLevel("SUPER");
        admin.setPermissions(List.of("ALL"));
        
        // Generate Teachers
        Teacher teacher1 = new Teacher();
        teacher1.setId(IDGenerator.generateUserId());
        teacher1.setUsername("amir.doe");
        teacher1.setPasswordHash(userService.hashPassword("teacher123"));
        teacher1.setFirstName("amir");
        teacher1.setLastName("Doe");
        teacher1.setEmail("amir.doe@university.edu");
        teacher1.setRole(UserRole.TEACHER);
        teacher1.setStatus(UserStatus.ACTIVE);
        teacher1.setEmployeeId("T001");
        teacher1.setDepartment("Computer Science");
        teacher1.setQualification("Ph.D. in Computer Science");
        teacher1.setYearsOfExperience(10);
        teacher1.setAreasOfExpertise(List.of("Algorithms", "Data Structures", "Machine Learning"));
        
        Teacher teacher2 = new Teacher();
        teacher2.setId(IDGenerator.generateUserId());
        teacher2.setUsername("jane.smith");
        teacher2.setPasswordHash(userService.hashPassword("teacher123"));
        teacher2.setFirstName("Jane");
        teacher2.setLastName("Smith");
        teacher2.setEmail("jane.smith@university.edu");
        teacher2.setRole(UserRole.TEACHER);
        teacher2.setStatus(UserStatus.ACTIVE);
        teacher2.setEmployeeId("T002");
        teacher2.setDepartment("Mathematics");
        teacher2.setQualification("Ph.D. in Mathematics");
        teacher2.setYearsOfExperience(8);
        teacher2.setAreasOfExpertise(List.of("Calculus", "Linear Algebra", "Statistics"));
        
        // Generate Students
        Student student1 = new Student();
        student1.setId(IDGenerator.generateUserId());
        student1.setUsername("student1");
        student1.setPasswordHash(userService.hashPassword("student123"));
        student1.setFirstName("Alice");
        student1.setLastName("amirson");
        student1.setEmail("alice.amirson@student.university.edu");
        student1.setRole(UserRole.STUDENT);
        student1.setStatus(UserStatus.ACTIVE);
        student1.setStudentId("S001");
        student1.setDepartment("Computer Science");
        student1.setSemester(3);
        student1.setCgpa(3.75);
        student1.setCreditsCompleted(45);
        
        Student student2 = new Student();
        student2.setId(IDGenerator.generateUserId());
        student2.setUsername("student2");
        student2.setPasswordHash(userService.hashPassword("student123"));
        student2.setFirstName("Bob");
        student2.setLastName("Williams");
        student2.setEmail("bob.williams@student.university.edu");
        student2.setRole(UserRole.STUDENT);
        student2.setStatus(UserStatus.ACTIVE);
        student2.setStudentId("S002");
        student2.setDepartment("Computer Science");
        student2.setSemester(3);
        student2.setCgpa(3.20);
        student2.setCreditsCompleted(42);
        
        // Generate Courses
        Course course1 = new Course();
        course1.setId(IDGenerator.generateCourseId());
        course1.setCourseCode("CS101");
        course1.setTitle("Introduction to Programming");
        course1.setDescription("Fundamentals of programming using Java");
        course1.setCredits(3);
        course1.setDepartment("Computer Science");
        course1.setSemester("Fall 2024");
        course1.setTeacherId(teacher1.getId());
        course1.setMaxStudents(30);
        course1.setCurrentEnrollment(2);
        course1.setStartDate(LocalDate.of(2024, 9, 1));
        course1.setEndDate(LocalDate.of(2024, 12, 20));
        course1.setSchedule("Mon Wed 10:00-11:30");
        course1.setClassroom("CS Building Room 101");
        course1.setPrerequisites(List.of());
        course1.setLearningOutcomes(List.of(
            "Understand basic programming concepts",
            "Write simple Java programs",
            "Debug and test code"
        ));
        
        Course course2 = new Course();
        course2.setId(IDGenerator.generateCourseId());
        course2.setCourseCode("MATH201");
        course2.setTitle("Calculus I");
        course2.setDescription("Introduction to differential and integral calculus");
        course2.setCredits(4);
        course2.setDepartment("Mathematics");
        course2.setSemester("Fall 2024");
        course2.setTeacherId(teacher2.getId());
        course2.setMaxStudents(25);
        course2.setCurrentEnrollment(2);
        course2.setStartDate(LocalDate.of(2024, 9, 1));
        course2.setEndDate(LocalDate.of(2024, 12, 20));
        course2.setSchedule("Tue Thu 13:00-14:30");
        course2.setClassroom("Math Building Room 205");
        course2.setPrerequisites(List.of());
        course2.setLearningOutcomes(List.of(
            "Understand limits and derivatives",
            "Solve integration problems",
            "Apply calculus to real-world problems"
        ));
        
        // Enroll students in courses
        student1.enrollInCourse(course1.getId());
        student1.enrollInCourse(course2.getId());
        student2.enrollInCourse(course1.getId());
        
        // Assign courses to teachers
        teacher1.assignCourse(course1.getId());
        teacher2.assignCourse(course2.getId());
        
        // Generate Assignments
        Assignment assignment1 = new Assignment();
        assignment1.setId(IDGenerator.generateAssignmentId());
        assignment1.setCourseId(course1.getId());
        assignment1.setTitle("Programming Assignment 1");
        assignment1.setDescription("Write a Java program that calculates factorial");
        assignment1.setType(AssignmentType.LAB);
        assignment1.setMaxScore(100);
        assignment1.setWeight(15);
        assignment1.setDueDate(LocalDateTime.of(2024, 10, 15, 23, 59));
        
        Assignment assignment2 = new Assignment();
        assignment2.setId(IDGenerator.generateAssignmentId());
        assignment2.setCourseId(course1.getId());
        assignment2.setTitle("Midterm Exam");
        assignment2.setDescription("Covers chapters 1-5");
        assignment2.setType(AssignmentType.EXAM);
        assignment2.setMaxScore(100);
        assignment2.setWeight(30);
        assignment2.setDueDate(LocalDateTime.of(2024, 11, 10, 23, 59));
        
        Assignment assignment3 = new Assignment();
        assignment3.setId(IDGenerator.generateAssignmentId());
        assignment3.setCourseId(course2.getId());
        assignment3.setTitle("Calculus Problem Set 1");
        assignment3.setDescription("Derivatives and limits problems");
        assignment3.setType(AssignmentType.HOMEWORK);
        assignment3.setMaxScore(50);
        assignment3.setWeight(10);
        assignment3.setDueDate(LocalDateTime.of(2024, 10, 20, 23, 59));
        
        // Generate Grades
        Grade grade1 = new Grade();
        grade1.setId(IDGenerator.generateGradeId());
        grade1.setStudentId(student1.getId());
        grade1.setCourseId(course1.getId());
        grade1.setAssignmentId(assignment1.getId());
        grade1.setScore(95);
        grade1.setMaxScore(100);
        grade1.setFeedback("Excellent work! Well-structured code.");
        grade1.setGradedBy(teacher1.getId());
        grade1.setGradedAt(LocalDateTime.now().minusDays(5));
        grade1.setPublished(true);
        grade1.calculateLetterGrade();
        
        Grade grade2 = new Grade();
        grade2.setId(IDGenerator.generateGradeId());
        grade2.setStudentId(student2.getId());
        grade2.setCourseId(course1.getId());
        grade2.setAssignmentId(assignment1.getId());
        grade2.setScore(85);
        grade2.setMaxScore(100);
        grade2.setFeedback("Good attempt. Improve code comments.");
        grade2.setGradedBy(teacher1.getId());
        grade2.setGradedAt(LocalDateTime.now().minusDays(4));
        grade2.setPublished(true);
        grade2.calculateLetterGrade();
        
        // Save all data
        dataManager.getUsers().put(admin.getId(), admin);
        dataManager.getUsers().put(teacher1.getId(), teacher1);
        dataManager.getUsers().put(teacher2.getId(), teacher2);
        dataManager.getUsers().put(student1.getId(), student1);
        dataManager.getUsers().put(student2.getId(), student2);
        
        dataManager.getCourses().put(course1.getId(), course1);
        dataManager.getCourses().put(course2.getId(), course2);
        
        dataManager.getAssignments().put(assignment1.getId(), assignment1);
        dataManager.getAssignments().put(assignment2.getId(), assignment2);
        dataManager.getAssignments().put(assignment3.getId(), assignment3);
        
        dataManager.getGrades().put(grade1.getId(), grade1);
        dataManager.getGrades().put(grade2.getId(), grade2);
        
        // Save to file
        
        System.out.println("Sample data generated successfully!");
        System.out.println("Admin: admin / admin123");
        System.out.println("Teacher: amir.doe / teacher123");
        System.out.println("Student: student1 / student123");
    }
}
