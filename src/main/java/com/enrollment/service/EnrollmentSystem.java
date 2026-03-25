package com.enrollment.service;

import com.enrollment.model.Course;
import com.enrollment.model.Student;

import java.util.*;

/**
 * Core business logic for the course enrollment system.
 * Manages students and courses and enforces enrollment rules.
 */
public class EnrollmentSystem {

    private static final double TUITION_PER_CREDIT = 300.0;

    private final Map<String, Student> students = new LinkedHashMap<>();
    private final Map<String, Course> courses = new LinkedHashMap<>();

    // -----------------------------------------------------------------------
    // Student management
    // -----------------------------------------------------------------------

    /** Adds a new student. Returns false when the student ID already exists. */
    public boolean addStudent(Student student) {
        if (student == null || students.containsKey(student.getId())) {
            return false;
        }
        students.put(student.getId(), student);
        return true;
    }

    /** Returns the student with the given ID, or null if not found. */
    public Student getStudent(String id) {
        return students.get(id);
    }

    /** Updates an existing student's name and major. Returns false if not found. */
    public boolean updateStudent(String id, String newName, String newMajor) {
        Student student = students.get(id);
        if (student == null) {
            return false;
        }
        if (newName != null && !newName.isBlank()) {
            student.setName(newName);
        }
        if (newMajor != null && !newMajor.isBlank()) {
            student.setMajor(newMajor);
        }
        return true;
    }

    /** Returns an unmodifiable view of all students. */
    public Collection<Student> getAllStudents() {
        return Collections.unmodifiableCollection(students.values());
    }

    // -----------------------------------------------------------------------
    // Course management
    // -----------------------------------------------------------------------

    /** Adds a new course to the catalog. Returns false when the course code already exists. */
    public boolean addCourse(Course course) {
        if (course == null || courses.containsKey(course.getCode())) {
            return false;
        }
        courses.put(course.getCode(), course);
        return true;
    }

    /** Returns the course with the given code, or null if not found. */
    public Course getCourse(String code) {
        return courses.get(code);
    }

    /** Updates editable fields of an existing course. Returns false if not found. */
    public boolean updateCourse(String code, String newTitle, Integer newCredits, Integer newCapacity) {
        Course course = courses.get(code);
        if (course == null) {
            return false;
        }
        if (newTitle != null && !newTitle.isBlank()) {
            course.setTitle(newTitle);
        }
        if (newCredits != null && newCredits > 0) {
            course.setCredits(newCredits);
        }
        if (newCapacity != null && newCapacity > 0) {
            course.setCapacity(newCapacity);
        }
        return true;
    }

    /** Returns an unmodifiable view of all courses. */
    public Collection<Course> getAllCourses() {
        return Collections.unmodifiableCollection(courses.values());
    }

    // -----------------------------------------------------------------------
    // Enrollment logic
    // -----------------------------------------------------------------------

    /**
     * Attempts to register the student in the course.
     *
     * @return an {@link EnrollmentResult} indicating success or the reason for failure.
     */
    public EnrollmentResult registerCourse(String studentId, String courseCode) {
        Student student = students.get(studentId);
        if (student == null) {
            return EnrollmentResult.failure("Student not found: " + studentId);
        }

        Course course = courses.get(courseCode);
        if (course == null) {
            return EnrollmentResult.failure("Course not found: " + courseCode);
        }

        // Already enrolled check
        if (student.isEnrolledIn(courseCode)) {
            return EnrollmentResult.failure("You are already enrolled in " + courseCode + ".");
        }

        // Capacity check
        if (course.isFull()) {
            return EnrollmentResult.failure("Course " + courseCode + " is full (capacity: "
                    + course.getCapacity() + ").");
        }

        // Prerequisite check
        for (String prereq : course.getPrerequisites()) {
            if (!student.hasCompleted(prereq)) {
                Course prereqCourse = courses.get(prereq);
                String prereqTitle = prereqCourse != null ? prereqCourse.getTitle() : prereq;
                return EnrollmentResult.failure(
                        "Prerequisite not met: you must complete \"" + prereqTitle
                                + "\" (" + prereq + ") before enrolling in " + courseCode + ".");
            }
        }

        // Time conflict check
        for (String enrolledCode : student.getEnrolledCourses()) {
            Course enrolled = courses.get(enrolledCode);
            if (enrolled != null && enrolled.getTimeSlot() != null && course.getTimeSlot() != null) {
                if (enrolled.getTimeSlot().overlaps(course.getTimeSlot())) {
                    return EnrollmentResult.failure(
                            "Schedule conflict: " + courseCode + " (" + course.getTimeSlot()
                                    + ") overlaps with " + enrolledCode
                                    + " (" + enrolled.getTimeSlot() + ").");
                }
            }
        }

        // All checks passed — perform enrollment
        student.enrollIn(courseCode);
        course.enrollStudent(studentId);
        return EnrollmentResult.success("Successfully enrolled in " + courseCode
                + " – " + course.getTitle() + ".");
    }

    /**
     * Drops the student from the course.
     *
     * @return an {@link EnrollmentResult} indicating success or the reason for failure.
     */
    public EnrollmentResult dropCourse(String studentId, String courseCode) {
        Student student = students.get(studentId);
        if (student == null) {
            return EnrollmentResult.failure("Student not found: " + studentId);
        }

        Course course = courses.get(courseCode);
        if (course == null) {
            return EnrollmentResult.failure("Course not found: " + courseCode);
        }

        if (!student.isEnrolledIn(courseCode)) {
            return EnrollmentResult.failure("You are not enrolled in " + courseCode + ".");
        }

        student.dropCourse(courseCode);
        course.removeStudent(studentId);
        return EnrollmentResult.success("Successfully dropped " + courseCode
                + " – " + course.getTitle() + ".");
    }

    // -----------------------------------------------------------------------
    // Reporting
    // -----------------------------------------------------------------------

    /**
     * Returns the list of courses a student is enrolled in,
     * or an empty list if the student does not exist.
     */
    public List<Course> getStudentSchedule(String studentId) {
        Student student = students.get(studentId);
        if (student == null) {
            return Collections.emptyList();
        }
        List<Course> schedule = new ArrayList<>();
        for (String code : student.getEnrolledCourses()) {
            Course c = courses.get(code);
            if (c != null) {
                schedule.add(c);
            }
        }
        return schedule;
    }

    /**
     * Returns the list of students enrolled in the given course,
     * or an empty list if the course does not exist.
     */
    public List<Student> getCourseRoster(String courseCode) {
        Course course = courses.get(courseCode);
        if (course == null) {
            return Collections.emptyList();
        }
        List<Student> roster = new ArrayList<>();
        for (String sid : course.getEnrolledStudents()) {
            Student s = students.get(sid);
            if (s != null) {
                roster.add(s);
            }
        }
        return roster;
    }

    /**
     * Calculates the total tuition for a student based on enrolled credits.
     * Returns -1.0 if the student is not found.
     */
    public double calculateTuition(String studentId) {
        Student student = students.get(studentId);
        if (student == null) {
            return -1.0;
        }
        int totalCredits = 0;
        for (String code : student.getEnrolledCourses()) {
            Course c = courses.get(code);
            if (c != null) {
                totalCredits += c.getCredits();
            }
        }
        return totalCredits * TUITION_PER_CREDIT;
    }

    // -----------------------------------------------------------------------
    // Raw map access for DataManager serialization
    // -----------------------------------------------------------------------

    public Map<String, Student> getStudentsMap() {
        return students;
    }

    public Map<String, Course> getCoursesMap() {
        return courses;
    }

    // -----------------------------------------------------------------------
    // Inner result type
    // -----------------------------------------------------------------------

    /** Immutable result of an enrollment or drop operation. */
    public static class EnrollmentResult {
        private final boolean success;
        private final String message;

        private EnrollmentResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static EnrollmentResult success(String message) {
            return new EnrollmentResult(true, message);
        }

        public static EnrollmentResult failure(String message) {
            return new EnrollmentResult(false, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}
