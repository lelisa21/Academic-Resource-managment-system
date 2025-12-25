package com.arms.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.arms.domain.Enrollment;
import com.arms.domain.enums.EnrollmentStatus;
import com.arms.persistence.DataManager;

public class EnrollmentService {
    
    private static EnrollmentService instance;
    private final DataManager dataManager;
    
    private EnrollmentService() {
        this.dataManager = DataManager.getInstance();
    }
    
    public static EnrollmentService getInstance() {
        if (instance == null) {
            instance = new EnrollmentService();
        }
        return instance;
    }
    
    public boolean enrollStudentInCourse(String studentId, String courseId, LocalDateTime enrolledAt) {
        try {
            // Check if enrollment already exists
            Optional<Enrollment> existingEnrollment = getEnrollment(studentId, courseId);
            if (existingEnrollment.isPresent() && 
                existingEnrollment.get().getStatus() == EnrollmentStatus.ENROLLED) {
                return false;
            }
            
            // Create new enrollment
            Enrollment enrollment = new Enrollment();
            enrollment.setId(java.util.UUID.randomUUID().toString());
            enrollment.setStudentId(studentId);
            enrollment.setCourseId(courseId);
            enrollment.setEnrolledAt(enrolledAt);
            enrollment.setStatus(EnrollmentStatus.ENROLLED);
            
            // Save through DataManager
            dataManager.saveEnrollment(enrollment);
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean dropCourse(String studentId, String courseId) {
        try {
            Optional<Enrollment> enrollmentOpt = getEnrollment(studentId, courseId);
            
            if (enrollmentOpt.isPresent() && 
                enrollmentOpt.get().getStatus() == EnrollmentStatus.ENROLLED) {
                Enrollment enrollment = enrollmentOpt.get();
                enrollment.setStatus(EnrollmentStatus.DROPPED);
                dataManager.saveEnrollment(enrollment);
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public Optional<Enrollment> getEnrollment(String studentId, String courseId) {
        try {
            return dataManager.getEnrollments().values().stream()
                    .filter(e -> e.getStudentId().equals(studentId) && 
                                e.getCourseId().equals(courseId))
                    .findFirst();
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
    
    public List<Enrollment> getEnrollmentsByStudent(String studentId) {
        try {
            return dataManager.getEnrollments().values().stream()
                    .filter(e -> e.getStudentId().equals(studentId))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
    
    public List<Enrollment> getEnrollmentsByCourse(String courseId) {
        try {
            return dataManager.getEnrollments().values().stream()
                    .filter(e -> e.getCourseId().equals(courseId))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
    
    public boolean updateEnrollmentStatus(String enrollmentId, EnrollmentStatus status) {
        try {
            Enrollment enrollment = dataManager.getEnrollments().get(enrollmentId);
            
            if (enrollment != null) {
                enrollment.setStatus(status);
                
                if (status == EnrollmentStatus.COMPLETED) {
                    enrollment.setCompletedAt(LocalDateTime.now());
                }
                
                dataManager.saveEnrollment(enrollment);
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public List<Enrollment> loadEnrollments() {
        return new java.util.ArrayList<>(dataManager.getEnrollments().values());
    }
    
    public void saveEnrollments(List<Enrollment> enrollments) {
        for (Enrollment enrollment : enrollments) {
            dataManager.saveEnrollment(enrollment);
        }
    }
}
