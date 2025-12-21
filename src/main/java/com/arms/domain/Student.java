package com.arms.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Student extends User {

    private String studentId;
    private String department;
    private int semester;
    private double cgpa;
    private List<String> enrolledCourseIds;
    private List<String> completedCourseIds;
    private int creditsCompleted;

    public Student() {
        super();
        this.enrolledCourseIds = new ArrayList<>();
        this.completedCourseIds = new ArrayList<>();
    }

    public Student(String studentId, String department, int semester, double cgpa,
            List<String> enrolledCourseIds, List<String> completedCourseIds,
            int creditsCompleted) {
        super();
        this.studentId = studentId;
        this.department = department;
        this.semester = semester;
        this.cgpa = cgpa;
        this.enrolledCourseIds = enrolledCourseIds == null ? new ArrayList<>() : enrolledCourseIds;
        this.completedCourseIds = completedCourseIds == null ? new ArrayList<>() : completedCourseIds;
        this.creditsCompleted = creditsCompleted;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public double getCgpa() {
        return cgpa;
    }

    public void setCgpa(double cgpa) {
        this.cgpa = cgpa;
    }

    public List<String> getEnrolledCourseIds() {
        return enrolledCourseIds;
    }

    public void setEnrolledCourseIds(List<String> enrolledCourseIds) {
        this.enrolledCourseIds = enrolledCourseIds;
    }

    public List<String> getCompletedCourseIds() {
        return completedCourseIds;
    }

    public void setCompletedCourseIds(List<String> completedCourseIds) {
        this.completedCourseIds = completedCourseIds;
    }

    public int getCreditsCompleted() {
        return creditsCompleted;
    }

    public void setCreditsCompleted(int creditsCompleted) {
        this.creditsCompleted = creditsCompleted;
    }

    public void enrollInCourse(String courseId) {
        if (enrolledCourseIds == null) {
            enrolledCourseIds = new ArrayList<>();
        }
        if (!enrolledCourseIds.contains(courseId)) {
            enrolledCourseIds.add(courseId);
        }
    }

    public void completeCourse(String courseId) {
        if (enrolledCourseIds != null) {
            enrolledCourseIds.remove(courseId);
        }
        if (completedCourseIds == null) {
            completedCourseIds = new ArrayList<>();
        }
        if (!completedCourseIds.contains(courseId)) {
            completedCourseIds.add(courseId);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Student)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        Student student = (Student) o;
        return Objects.equals(studentId, student.studentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), studentId);
    }
}
