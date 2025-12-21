package com.arms.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.arms.domain.Assignment;
import com.arms.domain.Course;
import com.arms.domain.Grade;
import com.arms.domain.Student;
import com.arms.domain.enums.AssignmentStatus;
import com.arms.persistence.DataManager;
import com.arms.persistence.IDGenerator;

public class AssignmentService {
    private static AssignmentService instance;
    private final DataManager dataManager;
    private final GradeService gradeService;

    private AssignmentService() {
        this.dataManager = DataManager.getInstance();
        this.gradeService = GradeService.getInstance();
    }

    public static synchronized AssignmentService getInstance() {
        if (instance == null) {
            instance = new AssignmentService();
        }
        return instance;
    }

    public Optional<Assignment> getAssignmentById(String id) {
        return Optional.ofNullable(dataManager.getAssignments().get(id));
    }

    public List<Assignment> getAllAssignments() {
        return new ArrayList<>(dataManager.getAssignments().values());
    }

    public List<Assignment> getAssignmentsByTeacher(String teacherId) {
        // Get courses taught by teacher
        CourseService courseService = CourseService.getInstance();
        List<String> courseIds = courseService.getCoursesByTeacher(teacherId).stream()
                .map(Course::getId)
                .collect(Collectors.toList());
        
        return dataManager.getAssignments().values().stream()
                .filter(assignment -> courseIds.contains(assignment.getCourseId()))
                .collect(Collectors.toList());
    }

    public List<Assignment> getAssignmentsForStudent(String studentId) {
        // Get courses student is enrolled in
        CourseService courseService = CourseService.getInstance();
        List<String> courseIds = courseService.getCoursesByStudent(studentId).stream()
                .map(Course::getId)
                .collect(Collectors.toList());
        
        return dataManager.getAssignments().values().stream()
                .filter(assignment -> courseIds.contains(assignment.getCourseId()))
                .collect(Collectors.toList());
    }

    public List<Assignment> getAssignmentsByCourse(String courseId) {
        return dataManager.getAssignments().values().stream()
                .filter(assignment -> assignment.getCourseId().equals(courseId))
                .sorted(Comparator.comparing(Assignment::getDueDate))
                .collect(Collectors.toList());
    }

    public List<Assignment> getActiveAssignmentsByCourse(String courseId) {
        return getAssignmentsByCourse(courseId).stream()
                .filter(assignment -> assignment.getStatus() == AssignmentStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    public Optional<Assignment> createAssignment(Assignment assignment) {
        if (assignment.getId() == null) {
            assignment.setId(IDGenerator.generateAssignmentId());
        }
        
        // Set default values
        if (assignment.getCreatedAt() == null) {
            assignment.setCreatedAt(LocalDateTime.now());
        }
        
        if (assignment.getStatus() == null) {
            assignment.setStatus(AssignmentStatus.ACTIVE);
        }
        
        if (assignment.getSubmissionStudentIds() == null) {
            assignment.setSubmissionStudentIds(new ArrayList<>());
        }
        
        if (assignment.getAttachments() == null) {
            assignment.setAttachments(new ArrayList<>());
        }
        
        dataManager.saveAssignment(assignment);
        return Optional.of(assignment);
    }

    public boolean updateAssignment(Assignment assignment) {
        Assignment existing = dataManager.getAssignments().get(assignment.getId());
        if (existing == null) {
            return false;
        }
        
        // Preserve submission list
        if (assignment.getSubmissionStudentIds() == null) {
            assignment.setSubmissionStudentIds(existing.getSubmissionStudentIds());
        }
        
        dataManager.saveAssignment(assignment);
        return true;
    }

    public boolean deleteAssignment(String assignmentId) {
        Assignment assignment = dataManager.getAssignments().get(assignmentId);
        if (assignment == null) {
            return false;
        }
        
        // Check if any submissions exist
        if (!assignment.getSubmissionStudentIds().isEmpty()) {
            return false;
        }
        
        dataManager.deleteAssignment(assignmentId);
        return true;
    }

    public boolean submitAssignment(String assignmentId, String studentId, double score, String submissionContent) {
        Assignment assignment = dataManager.getAssignments().get(assignmentId);
        if (assignment == null || !assignment.canSubmit(studentId)) {
            return false;
        }
        
        // Add student to submission list
        assignment.getSubmissionStudentIds().add(studentId);
        
        // Create or update grade
        Optional<Grade> existingGrade = gradeService.getGradeByStudentAndAssignment(studentId, assignmentId);
        
        if (existingGrade.isPresent()) {
            // Update existing grade
            Grade grade = existingGrade.get();
            grade.setScore(score);
            grade.setMaxScore(assignment.getMaxScore());
            grade.calculateLetterGrade();
            grade.setGradedAt(LocalDateTime.now());
            dataManager.saveGrade(grade);
        } else {
            // Create new grade
            gradeService.createGrade(studentId, assignment.getCourseId(), assignmentId, score, assignment.getMaxScore());
        }
        
        dataManager.saveAssignment(assignment);
        return true;
    }

    public List<Assignment> getUpcomingAssignments(String studentId, int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.plusDays(days);
        
        return getAssignmentsForStudent(studentId).stream()
                .filter(assignment -> 
                    assignment.getDueDate() != null &&
                    assignment.getDueDate().isAfter(now) &&
                    assignment.getDueDate().isBefore(deadline) &&
                    assignment.canSubmit(studentId))
                .sorted(Comparator.comparing(Assignment::getDueDate))
                .collect(Collectors.toList());
    }

    public List<Assignment> getOverdueAssignments(String studentId) {
        LocalDateTime now = LocalDateTime.now();
        
        return getAssignmentsForStudent(studentId).stream()
                .filter(assignment -> 
                    assignment.isOverdue() &&
                    assignment.canSubmit(studentId))
                .sorted(Comparator.comparing(Assignment::getDueDate))
                .collect(Collectors.toList());
    }

    public Map<String, Integer> getSubmissionStats(String assignmentId) {
        Assignment assignment = dataManager.getAssignments().get(assignmentId);
        if (assignment == null) {
            return Map.of("submitted", 0, "total", 0, "percentage", 0);
        }
        
        CourseService courseService = CourseService.getInstance();
        int totalStudents = courseService.getEnrolledStudents(assignment.getCourseId()).size();
        int submitted = assignment.getSubmissionStudentIds().size();
        int percentage = totalStudents > 0 ? (submitted * 100) / totalStudents : 0;
        
        return Map.of(
            "submitted", submitted,
            "total", totalStudents,
            "percentage", percentage
        );
    }

    public boolean publishAssignment(String assignmentId) {
        Assignment assignment = dataManager.getAssignments().get(assignmentId);
        if (assignment == null) {
            return false;
        }
        
        assignment.setStatus(AssignmentStatus.ARCHIVED);
        dataManager.saveAssignment(assignment);
        return true;
    }

    public boolean closeAssignment(String assignmentId) {
        Assignment assignment = dataManager.getAssignments().get(assignmentId);
        if (assignment == null) {
            return false;
        }
        
        assignment.setStatus(AssignmentStatus.SUBMISSION_CLOSED);
        dataManager.saveAssignment(assignment);
        return true;
    }

    public List<Student> getStudentsWhoSubmitted(String id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void gradeAssignment(String id, String id0, double marks, String comment, String id1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
