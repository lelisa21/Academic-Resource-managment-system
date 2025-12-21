package com.arms.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Teacher extends User {

    private String employeeId;
    private String department;
    private String qualification;
    private List<String> assignedCourseIds;
    private List<String> areasOfExpertise;
    private int yearsOfExperience;

    public Teacher() {
        super();
        this.assignedCourseIds = new ArrayList<>();
        this.areasOfExpertise = new ArrayList<>();
    }

    public Teacher(String employeeId, String department, String qualification,
            List<String> assignedCourseIds, List<String> areasOfExpertise,
            int yearsOfExperience) {
        super();
        this.employeeId = employeeId;
        this.department = department;
        this.qualification = qualification;
        this.assignedCourseIds = assignedCourseIds == null ? new ArrayList<>() : assignedCourseIds;
        this.areasOfExpertise = areasOfExpertise == null ? new ArrayList<>() : areasOfExpertise;
        this.yearsOfExperience = yearsOfExperience;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public List<String> getAssignedCourseIds() {
        return assignedCourseIds;
    }

    public void setAssignedCourseIds(List<String> assignedCourseIds) {
        this.assignedCourseIds = assignedCourseIds;
    }

    public List<String> getAreasOfExpertise() {
        return areasOfExpertise;
    }

    public void setAreasOfExpertise(List<String> areasOfExpertise) {
        this.areasOfExpertise = areasOfExpertise;
    }

    public int getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(int yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public void assignCourse(String courseId) {
        if (assignedCourseIds == null) {
            assignedCourseIds = new ArrayList<>();
        }
        if (!assignedCourseIds.contains(courseId)) {
            assignedCourseIds.add(courseId);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Teacher)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        Teacher teacher = (Teacher) o;
        return Objects.equals(employeeId, teacher.employeeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), employeeId);
    }
}
