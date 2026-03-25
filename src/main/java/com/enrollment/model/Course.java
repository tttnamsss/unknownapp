package com.enrollment.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a course in the catalog.
 */
public class Course {

    private String code;
    private String title;
    private int credits;
    private int capacity;
    private TimeSlot timeSlot;
    private List<String> prerequisites; // course codes required before enrolling
    private List<String> enrolledStudents; // student IDs currently enrolled

    public Course() {
        this.prerequisites = new ArrayList<>();
        this.enrolledStudents = new ArrayList<>();
    }

    public Course(String code, String title, int credits, int capacity, TimeSlot timeSlot) {
        this.code = code;
        this.title = title;
        this.credits = credits;
        this.capacity = capacity;
        this.timeSlot = timeSlot;
        this.prerequisites = new ArrayList<>();
        this.enrolledStudents = new ArrayList<>();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    public List<String> getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(List<String> prerequisites) {
        this.prerequisites = prerequisites != null ? prerequisites : new ArrayList<>();
    }

    public List<String> getEnrolledStudents() {
        return enrolledStudents;
    }

    public void setEnrolledStudents(List<String> enrolledStudents) {
        this.enrolledStudents = enrolledStudents != null ? enrolledStudents : new ArrayList<>();
    }

    /** Returns the number of students currently enrolled. */
    public int getEnrollmentCount() {
        return enrolledStudents.size();
    }

    /** Returns true when the course has no remaining spots. */
    public boolean isFull() {
        return enrolledStudents.size() >= capacity;
    }

    /** Returns the number of open seats remaining. */
    public int getAvailableSeats() {
        return Math.max(0, capacity - enrolledStudents.size());
    }

    /** Returns true when the given student ID is already enrolled. */
    public boolean hasStudent(String studentId) {
        return enrolledStudents.contains(studentId);
    }

    /** Enrolls a student, returning false if the course is full or already enrolled. */
    public boolean enrollStudent(String studentId) {
        if (isFull() || hasStudent(studentId)) {
            return false;
        }
        enrolledStudents.add(studentId);
        return true;
    }

    /** Removes a student from this course. Returns false if the student was not enrolled. */
    public boolean removeStudent(String studentId) {
        return enrolledStudents.remove(studentId);
    }

    @Override
    public String toString() {
        String prereqStr = prerequisites.isEmpty() ? "None" : String.join(", ", prerequisites);
        String timeStr = timeSlot != null ? timeSlot.toString() : "TBA";
        return String.format("%-10s %-40s Credits: %d  Capacity: %d/%d  Time: %-18s  Prerequisites: %s",
                code, title, credits, enrolledStudents.size(), capacity, timeStr, prereqStr);
    }
}
