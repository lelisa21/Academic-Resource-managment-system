package com.arms.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.arms.domain.Course;
import com.arms.domain.Enrollment;
import com.arms.domain.Student;
import com.arms.domain.enums.EnrollmentStatus;
import com.arms.persistence.DataManager;
import com.arms.persistence.IDGenerator;

public class CourseService {
    private static CourseService instance;
    private final DataManager dataManager;

    private CourseService() {
        this.dataManager = DataManager.getInstance();
    }

    public static synchronized CourseService getInstance() {
        if (instance == null) {
            instance = new CourseService();
        }
        return instance;
    }

    public Optional<Course> getCourseById(String id) {
        return Optional.ofNullable(dataManager.getCourses().get(id));
    }

    public List<Course> getAllCourses() {
        return new ArrayList<>(dataManager.getCourses().values());
    }

    public List<Course> getActiveCourses() {
        return dataManager.getCourses().values().stream()
                .filter(Course::isActive)
                .collect(Collectors.toList());
    }

    public List<Course> getCoursesByTeacher(String teacherId) {
        return dataManager.getCourses().values().stream()
                .filter(course -> course.getTeacherId() != null && course.getTeacherId().equals(teacherId))
                .collect(Collectors.toList());
    }

    public List<Course> getCoursesByDepartment(String department) {
        return dataManager.getCourses().values().stream()
                .filter(course -> course.getDepartment() != null && course.getDepartment().equalsIgnoreCase(department))
                .collect(Collectors.toList());
    }

    public List<Course> getCoursesBySemester(String semester) {
        return dataManager.getCourses().values().stream()
                .filter(course -> course.getSemester() != null && course.getSemester().equalsIgnoreCase(semester))
                .collect(Collectors.toList());
    }

    public List<Course> getCoursesByStudent(String studentId) {
        List<String> enrolledCourseIds = dataManager.getEnrollments().values().stream()
                .filter(enrollment -> enrollment.getStudentId().equals(studentId))
                .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.ENROLLED)
                .map(Enrollment::getCourseId)
                .collect(Collectors.toList());
        
        return enrolledCourseIds.stream()
                .map(courseId -> dataManager.getCourses().get(courseId))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<Student> getEnrolledStudents(String courseId) {
        List<String> studentIds = dataManager.getEnrollments().values().stream()
                .filter(enrollment -> enrollment.getCourseId().equals(courseId))
                .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.ENROLLED)
                .map(Enrollment::getStudentId)
                .collect(Collectors.toList());
        
        return studentIds.stream()
                .map(studentId -> (Student) dataManager.getUsers().get(studentId))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Optional<Course> createCourse(Course course) {
        if (course.getId() == null) {
            course.setId(IDGenerator.generateCourseId());
        }
        
        // Set default values
        if (course.getCurrentEnrollment() == 0) {
            course.setCurrentEnrollment(0);
        }
        
        if (course.isActive() && course.getCurrentEnrollment() < course.getMaxStudents()) {
            dataManager.saveCourse(course);
            return Optional.of(course);
        }
        
        return Optional.empty();
    }

    public boolean updateCourse(Course course) {
        Course existing = dataManager.getCourses().get(course.getId());
        if (existing == null) {
            return false;
        }
        
        // Preserve enrollment count
        course.setCurrentEnrollment(existing.getCurrentEnrollment());
        
        dataManager.saveCourse(course);
        return true;
    }

    public boolean deleteCourse(String courseId) {
        Course course = dataManager.getCourses().get(courseId);
        if (course == null) {
            return false;
        }
        
        // Check if any students are enrolled
        long enrolledStudents = getEnrolledStudents(courseId).size();
        if (enrolledStudents > 0) {
            return false;
        }
        
        dataManager.deleteCourse(courseId);
        return true;
    }

    public boolean enrollStudent(String studentId, String courseId) {
        Course course = dataManager.getCourses().get(courseId);
        Student student = (Student) dataManager.getUsers().get(studentId);
        
        if (course == null || student == null || !course.canEnroll()) {
            return false;
        }
        
        // Check if already enrolled
        boolean alreadyEnrolled = dataManager.getEnrollments().values().stream()
                .anyMatch(e -> e.getStudentId().equals(studentId) && 
                               e.getCourseId().equals(courseId) && 
                               e.getStatus() == EnrollmentStatus.ENROLLED);
        
        if (alreadyEnrolled) {
            return false;
        }
        
        // Create enrollment
        Enrollment enrollment = new Enrollment();
        enrollment.setId(IDGenerator.generateEnrollmentId());
        enrollment.setStudentId(studentId);
        enrollment.setCourseId(courseId);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setStatus(EnrollmentStatus.ENROLLED);
        
        // Update course enrollment count
        course.incrementEnrollment();
        
        // Update student's enrolled courses
        student.enrollInCourse(courseId);
        
        // Save all changes
        dataManager.saveEnrollment(enrollment);
        dataManager.saveCourse(course);
        dataManager.saveUser(student);
        
        return true;
    }

    public boolean dropStudent(String studentId, String courseId) {
        Optional<Enrollment> enrollmentOpt = dataManager.getEnrollments().values().stream()
                .filter(e -> e.getStudentId().equals(studentId) && 
                             e.getCourseId().equals(courseId) && 
                             e.getStatus() == EnrollmentStatus.ENROLLED)
                .findFirst();
        
        if (enrollmentOpt.isEmpty()) {
            return false;
        }
        
        Enrollment enrollment = enrollmentOpt.get();
        Course course = dataManager.getCourses().get(courseId);
        Student student = (Student) dataManager.getUsers().get(studentId);
        
        if (course == null || student == null) {
            return false;
        }
        
        // Update enrollment status
        enrollment.setStatus(EnrollmentStatus.DROPPED);
        
        // Update course enrollment count
        course.setCurrentEnrollment(Math.max(0, course.getCurrentEnrollment() - 1));
        
        // Update student's enrolled courses
        if (student.getEnrolledCourseIds() != null) {
            student.getEnrolledCourseIds().remove(courseId);
        }
        
        // Save all changes
        dataManager.saveEnrollment(enrollment);
        dataManager.saveCourse(course);
        dataManager.saveUser(student);
        
        return true;
    }

    public List<Course> searchCourses(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return dataManager.getCourses().values().stream()
                .filter(course -> 
                    course.getCourseCode().toLowerCase().contains(lowerKeyword) ||
                    course.getTitle().toLowerCase().contains(lowerKeyword) ||
                    course.getDescription().toLowerCase().contains(lowerKeyword) ||
                    (course.getDepartment() != null && course.getDepartment().toLowerCase().contains(lowerKeyword)))
                .collect(Collectors.toList());
    }
}
