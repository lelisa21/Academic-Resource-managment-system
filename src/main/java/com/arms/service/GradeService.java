package com.arms.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.arms.domain.Grade;
import com.arms.persistence.DataManager;
import com.arms.persistence.IDGenerator;

public class GradeService {
    private static GradeService instance;
    private final DataManager dataManager;

    private GradeService() {
        this.dataManager = DataManager.getInstance();
    }

    public static synchronized GradeService getInstance() {
        if (instance == null) {
            instance = new GradeService();
        }
        return instance;
    }

    public Optional<Grade> getGradeById(String id) {
        return Optional.ofNullable(dataManager.getGrades().get(id));
    }

    public List<Grade> getAllGrades() {
        return new ArrayList<>(dataManager.getGrades().values());
    }

    public List<Grade> getGradesByStudent(String studentId) {
        return dataManager.getGrades().values().stream()
                .filter(grade -> grade.getStudentId().equals(studentId))
                .collect(Collectors.toList());
    }

    public List<Grade> getGradesByCourse(String courseId) {
        return dataManager.getGrades().values().stream()
                .filter(grade -> grade.getCourseId().equals(courseId))
                .collect(Collectors.toList());
    }

    public List<Grade> getGradesByAssignment(String assignmentId) {
        return dataManager.getGrades().values().stream()
                .filter(grade -> grade.getAssignmentId().equals(assignmentId))
                .collect(Collectors.toList());
    }

    public Optional<Grade> getGradeByStudentAndAssignment(String studentId, String assignmentId) {
        return dataManager.getGrades().values().stream()
                .filter(grade -> grade.getStudentId().equals(studentId) && 
                                 grade.getAssignmentId().equals(assignmentId))
                .findFirst();
    }

    public List<Grade> getPublishedGradesByStudent(String studentId) {
        return getGradesByStudent(studentId).stream()
                .filter(Grade::isPublished)
                .collect(Collectors.toList());
    }

    public Optional<Grade> createGrade(String studentId, String courseId, String assignmentId, 
                                      double score, double maxScore) {
        Grade grade = new Grade();
        grade.setId(IDGenerator.generateGradeId());
        grade.setStudentId(studentId);
        grade.setCourseId(courseId);
        grade.setAssignmentId(assignmentId);
        grade.setScore(score);
        grade.setMaxScore(maxScore);
        grade.calculateLetterGrade();
        grade.setGradedAt(LocalDateTime.now());
        grade.setPublished(false);
        
        dataManager.saveGrade(grade);
        return Optional.of(grade);
    }

    public boolean updateGrade(String gradeId, double score, String feedback, boolean published) {
        Grade grade = dataManager.getGrades().get(gradeId);
        if (grade == null) {
            return false;
        }
        
        grade.setScore(score);
        grade.setFeedback(feedback);
        grade.setPublished(published);
        grade.calculateLetterGrade();
        grade.setGradedAt(LocalDateTime.now());
        
        dataManager.saveGrade(grade);
        return true;
    }

    public boolean deleteGrade(String gradeId) {
        Grade grade = dataManager.getGrades().get(gradeId);
        if (grade == null) {
            return false;
        }
        
        dataManager.deleteGrade(gradeId);
        return true;
    }

    public Optional<Double> calculateStudentAverage(String studentId) {
        List<Grade> grades = getPublishedGradesByStudent(studentId);
        
        if (grades.isEmpty()) {
            return Optional.empty();
        }
        
        double total = grades.stream()
                .mapToDouble(grade -> (grade.getScore() / grade.getMaxScore()) * 100)
                .average()
                .orElse(0.0);
        
        return Optional.of(total);
    }

    public Optional<Double> calculateCourseAverageForStudent(String studentId, String courseId) {
        List<Grade> grades = getGradesByStudent(studentId).stream()
                .filter(grade -> grade.getCourseId().equals(courseId))
                .filter(Grade::isPublished)
                .collect(Collectors.toList());
        
        if (grades.isEmpty()) {
            return Optional.empty();
        }
        
        double total = grades.stream()
                .mapToDouble(grade -> (grade.getScore() / grade.getMaxScore()) * 100)
                .average()
                .orElse(0.0);
        
        return Optional.of(total);
    }

    public Map<String, Double> calculateFinalGradesForCourse(String courseId) {
        Map<String, Double> finalGrades = new HashMap<>();
        
        // Group grades by student
        Map<String, List<Grade>> gradesByStudent = dataManager.getGrades().values().stream()
                .filter(grade -> grade.getCourseId().equals(courseId))
                .filter(Grade::isPublished)
                .collect(Collectors.groupingBy(Grade::getStudentId));
        
        // Calculate average for each student
        gradesByStudent.forEach((studentId, grades) -> {
            double average = grades.stream()
                    .mapToDouble(grade -> (grade.getScore() / grade.getMaxScore()) * 100)
                    .average()
                    .orElse(0.0);
            finalGrades.put(studentId, average);
        });
        
        return finalGrades;
    }

    public void publishGradesForAssignment(String assignmentId) {
        dataManager.getGrades().values().stream()
                .filter(grade -> grade.getAssignmentId().equals(assignmentId))
                .forEach(grade -> grade.setPublished(true));
        
        // Save changes
        dataManager.getGrades().values().stream()
                .filter(grade -> grade.getAssignmentId().equals(assignmentId))
                .forEach(dataManager::saveGrade);
    }

    public Map<String, Long> getGradeDistribution(String courseId) {
        return dataManager.getGrades().values().stream()
                .filter(grade -> grade.getCourseId().equals(courseId))
                .filter(Grade::isPublished)
                .collect(Collectors.groupingBy(
                    Grade::getLetterGrade,
                    Collectors.counting()
                ));
    }

    public double calculateClassAverage(String courseId) {
        List<Grade> grades = getGradesByCourse(courseId).stream()
                .filter(Grade::isPublished)
                .collect(Collectors.toList());
        
        if (grades.isEmpty()) {
            return 0.0;
        }
        
        return grades.stream()
                .mapToDouble(grade -> (grade.getScore() / grade.getMaxScore()) * 100)
                .average()
                .orElse(0.0);
    }

    public Object createOrUpdateGrade(Grade newGrade) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
