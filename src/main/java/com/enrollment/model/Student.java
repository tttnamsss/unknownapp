package com.enrollment.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a student in the enrollment system.
 */
public class Student {

    private String id;
    private String name;
    private String major;
    private List<String> enrolledCourses;  // course codes currently enrolled
    private List<String> completedCourses; // course codes already completed (for prerequisite checks)

    public Student() {
        this.enrolledCourses = new ArrayList<>();
        this.completedCourses = new ArrayList<>();
    }

    public Student(String id, String name, String major) {
        this.id = id;
        this.name = name;
        this.major = major;
        this.enrolledCourses = new ArrayList<>();
        this.completedCourses = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public List<String> getEnrolledCourses() {
        return enrolledCourses;
    }

    public void setEnrolledCourses(List<String> enrolledCourses) {
        this.enrolledCourses = enrolledCourses != null ? enrolledCourses : new ArrayList<>();
    }

    public List<String> getCompletedCourses() {
        return completedCourses;
    }

    public void setCompletedCourses(List<String> completedCourses) {
        this.completedCourses = completedCourses != null ? completedCourses : new ArrayList<>();
    }

    /** Returns true when the student is enrolled in the given course code. */
    public boolean isEnrolledIn(String courseCode) {
        return enrolledCourses.contains(courseCode);
    }

    /** Returns true when the student has completed the given course code. */
    public boolean hasCompleted(String courseCode) {
        return completedCourses.contains(courseCode);
    }

    /** Adds a course code to the enrolled list. Returns false if already enrolled. */
    public boolean enrollIn(String courseCode) {
        if (isEnrolledIn(courseCode)) {
            return false;
        }
        enrolledCourses.add(courseCode);
        return true;
    }

    /** Removes a course code from the enrolled list. Returns false if not enrolled. */
    public boolean dropCourse(String courseCode) {
        return enrolledCourses.remove(courseCode);
    }

    @Override
    public String toString() {
        return String.format("ID: %-12s  Name: %-25s  Major: %s", id, name, major);
    }
}
