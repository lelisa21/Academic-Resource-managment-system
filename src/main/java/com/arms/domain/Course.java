package com.arms.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Course {

    private String id;
    private String courseCode;
    private String title;
    private String description;
    private int credits;
    private String department;
    private String semester;
    private String teacherId;
    private int maxStudents;
    private int currentEnrollment;
    private LocalDate startDate;
    private LocalDate endDate;
    private String schedule;
    private String classroom;
    private List<String> prerequisites;
    private List<String> learningOutcomes;
    private boolean isActive;

    public Course() {
        this.prerequisites = new ArrayList<>();
        this.learningOutcomes = new ArrayList<>();
        this.isActive = true;
    }

    public Course(String id, String courseCode, String title, String description, int credits,
            String department, String semester, String teacherId, int maxStudents,
            int currentEnrollment, LocalDate startDate, LocalDate endDate, String schedule,
            String classroom, List<String> prerequisites, List<String> learningOutcomes,
            boolean isActive) {
        this.id = id;
        this.courseCode = courseCode;
        this.title = title;
        this.description = description;
        this.credits = credits;
        this.department = department;
        this.semester = semester;
        this.teacherId = teacherId;
        this.maxStudents = maxStudents;
        this.currentEnrollment = currentEnrollment;
        this.startDate = startDate;
        this.endDate = endDate;
        this.schedule = schedule;
        this.classroom = classroom;
        this.prerequisites = prerequisites == null ? new ArrayList<>() : prerequisites;
        this.learningOutcomes = learningOutcomes == null ? new ArrayList<>() : learningOutcomes;
        this.isActive = isActive;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
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

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public int getMaxStudents() {
        return maxStudents;
    }

    public void setMaxStudents(int maxStudents) {
        this.maxStudents = maxStudents;
    }

    public int getCurrentEnrollment() {
        return currentEnrollment;
    }

    public void setCurrentEnrollment(int currentEnrollment) {
        this.currentEnrollment = currentEnrollment;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getClassroom() {
        return classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }

    public List<String> getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(List<String> prerequisites) {
        this.prerequisites = prerequisites;
    }

    public List<String> getLearningOutcomes() {
        return learningOutcomes;
    }

    public void setLearningOutcomes(List<String> learningOutcomes) {
        this.learningOutcomes = learningOutcomes;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean canEnroll() {
        return isActive && currentEnrollment < maxStudents;
    }

    public void incrementEnrollment() {
        if (currentEnrollment < maxStudents) {
            currentEnrollment++;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Course course = (Course) o;
        return Objects.equals(id, course.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
