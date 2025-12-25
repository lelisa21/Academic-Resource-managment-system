package com.arms.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public class Grade {

    private String id;
    private String studentId;
    private String courseId;
    private String assignmentId;
    private double score;
    private double maxScore;
    private double percentage;
    private String letterGrade;
    private String feedback;
    private String gradedBy;
    private LocalDateTime gradedAt;
    private boolean isPublished;

    public Grade() {
    }

    public Grade(String id, String studentId, String courseId, String assignmentId, double score,
            double maxScore, double percentage, String letterGrade, String feedback,
            String gradedBy, LocalDateTime gradedAt, boolean isPublished) {
        this.id = id;
        this.studentId = studentId;
        this.courseId = courseId;
        this.assignmentId = assignmentId;
        this.score = score;
        this.maxScore = maxScore;
        this.percentage = percentage;
        this.letterGrade = letterGrade;
        this.feedback = feedback;
        this.gradedBy = gradedBy;
        this.gradedAt = gradedAt;
        this.isPublished = isPublished;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(String assignmentId) {
        this.assignmentId = assignmentId;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(double maxScore) {
        this.maxScore = maxScore;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public String getLetterGrade() {
        return letterGrade;
    }

    public void setLetterGrade(String letterGrade) {
        this.letterGrade = letterGrade;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getGradedBy() {
        return gradedBy;
    }

    public void setGradedBy(String gradedBy) {
        this.gradedBy = gradedBy;
    }

    public LocalDateTime getGradedAt() {
        return gradedAt;
    }

    public void setGradedAt(LocalDateTime gradedAt) {
        this.gradedAt = gradedAt;
    }

    public boolean isPublished() {
        return isPublished;
    }

    public void setPublished(boolean published) {
        isPublished = published;
    }

    public void calculateLetterGrade() {
        if (maxScore == 0) {
            return;
        }
        percentage = (score / maxScore) * 100;

        if (percentage >= 90) {
            letterGrade = "A+"; 
        }else if (percentage >= 85) {
            letterGrade = "A"; 
        }else if (percentage >= 75) {
            letterGrade = "B+"; 
        }else if (percentage >= 70) {
            letterGrade = "B"; 
        }else if (percentage >= 65) {
            letterGrade = "C+"; 
        }else if (percentage >= 60) {
            letterGrade = "C"; 
        }else if (percentage >= 50) {
            letterGrade = "D"; 
        }else {
            letterGrade = "F";
        }
    }

    public double getWeightedScore(double weight) {
        if (maxScore == 0) {
            return 0;
        }
        return (score / maxScore) * weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Grade grade = (Grade) o;
        return Objects.equals(id, grade.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
