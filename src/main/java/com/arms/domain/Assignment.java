package com.arms.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.arms.domain.enums.AssignmentStatus;
import com.arms.domain.enums.AssignmentType;

public class Assignment {

    private String id;
    private String courseId;
    private String title;
    private String description;
    private AssignmentType type;
    private double maxScore;
    private double weight;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private List<String> attachments;
    private AssignmentStatus status;
    private List<String> submissionStudentIds;
     private boolean overdue;

    public Assignment() {
        this.attachments = new ArrayList<>();
        this.submissionStudentIds = new ArrayList<>();
        this.status = AssignmentStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
    }

    public Assignment(String id, String courseId, String title, String description, AssignmentType type,
            double maxScore, double weight, LocalDateTime dueDate, LocalDateTime createdAt,
            List<String> attachments, AssignmentStatus status, List<String> submissionStudentIds) {
        this.id = id;
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.type = type;
        this.maxScore = maxScore;
        this.weight = weight;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.attachments = attachments == null ? new ArrayList<>() : attachments;
        this.status = status;
        this.submissionStudentIds = submissionStudentIds == null ? new ArrayList<>() : submissionStudentIds;
    }
    
    public void setOverdue(boolean overdue) {
        this.overdue = overdue;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AssignmentType getType() {
        return type;
    }

    public void setType(AssignmentType type) {
        this.type = type;
    }

    public double getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(double maxScore) {
        this.maxScore = maxScore;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }

    public AssignmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssignmentStatus status) {
        this.status = status;
    }

    public List<String> getSubmissionStudentIds() {
        return submissionStudentIds;
    }

    public void setSubmissionStudentIds(List<String> submissionStudentIds) {
        this.submissionStudentIds = submissionStudentIds;
    }

    public boolean isOverdue() {
        return dueDate != null && LocalDateTime.now().isAfter(dueDate);
    }

    public boolean canSubmit(String studentId) {
        return status == AssignmentStatus.ACTIVE && !isOverdue() && (submissionStudentIds == null || !submissionStudentIds.contains(studentId));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Assignment that = (Assignment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
