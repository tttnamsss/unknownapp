package com.enrollment.service;

import com.enrollment.model.Course;
import com.enrollment.model.Student;
import com.enrollment.model.TimeSlot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;

/**
 * Handles loading and saving of students and courses to JSON files.
 *
 * <p>Data is stored in the {@code data/} directory relative to the working directory:
 * <ul>
 *   <li>{@code data/students.json}</li>
 *   <li>{@code data/courses.json}</li>
 * </ul>
 */
public class DataManager {

    private static final String DATA_DIR = "data";
    private static final String STUDENTS_FILE = DATA_DIR + "/students.json";
    private static final String COURSES_FILE = DATA_DIR + "/courses.json";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Saves the current state (students and courses) to JSON files.
     * Creates the {@code data/} directory if it does not exist.
     */
    public void saveData(EnrollmentSystem system) throws IOException {
        Files.createDirectories(Paths.get(DATA_DIR));

        // Save students
        List<Student> studentList = new ArrayList<>(system.getAllStudents());
        try (Writer writer = new FileWriter(STUDENTS_FILE)) {
            gson.toJson(studentList, writer);
        }

        // Save courses
        List<Course> courseList = new ArrayList<>(system.getAllCourses());
        try (Writer writer = new FileWriter(COURSES_FILE)) {
            gson.toJson(courseList, writer);
        }
    }

    /**
     * Loads students and courses from JSON files into the provided system.
     * If the files do not exist, no data is loaded (the system remains empty).
     */
    public void loadData(EnrollmentSystem system) throws IOException {
        Path studentsPath = Paths.get(STUDENTS_FILE);
        Path coursesPath = Paths.get(COURSES_FILE);

        // Load courses first (students reference course codes)
        if (Files.exists(coursesPath)) {
            try (Reader reader = new FileReader(coursesPath.toFile())) {
                Type courseListType = new TypeToken<List<Course>>() {}.getType();
                List<Course> courses = gson.fromJson(reader, courseListType);
                if (courses != null) {
                    for (Course course : courses) {
                        // Ensure non-null lists after deserialization
                        if (course.getPrerequisites() == null) {
                            course.setPrerequisites(new ArrayList<>());
                        }
                        if (course.getEnrolledStudents() == null) {
                            course.setEnrolledStudents(new ArrayList<>());
                        }
                        system.getCoursesMap().put(course.getCode(), course);
                    }
                }
            }
        }

        // Load students
        if (Files.exists(studentsPath)) {
            try (Reader reader = new FileReader(studentsPath.toFile())) {
                Type studentListType = new TypeToken<List<Student>>() {}.getType();
                List<Student> students = gson.fromJson(reader, studentListType);
                if (students != null) {
                    for (Student student : students) {
                        if (student.getEnrolledCourses() == null) {
                            student.setEnrolledCourses(new ArrayList<>());
                        }
                        if (student.getCompletedCourses() == null) {
                            student.setCompletedCourses(new ArrayList<>());
                        }
                        system.getStudentsMap().put(student.getId(), student);
                    }
                }
            }
        }
    }

    /**
     * Seeds the system with a default course catalog and sample students
     * when no data files exist yet. This gives users something to explore
     * on first launch.
     */
    public void seedDefaultData(EnrollmentSystem system) {
        // --- Default courses ---
        system.addCourse(new Course("CS101", "Intro to Programming",
                3, 30, new TimeSlot("MWF", "09:00", "10:00")));
        system.addCourse(new Course("CS201", "Data Structures",
                3, 25, new TimeSlot("MWF", "10:00", "11:00")));
        system.addCourse(new Course("CS301", "Algorithms",
                3, 25, new TimeSlot("TTh", "09:00", "10:30")));
        system.addCourse(new Course("CS401", "Operating Systems",
                3, 20, new TimeSlot("TTh", "10:30", "12:00")));
        system.addCourse(new Course("MATH101", "Calculus I",
                4, 35, new TimeSlot("MWF", "08:00", "09:00")));
        system.addCourse(new Course("MATH201", "Calculus II",
                4, 30, new TimeSlot("MWF", "11:00", "12:00")));
        system.addCourse(new Course("ENG101", "Technical Writing",
                2, 40, new TimeSlot("TTh", "13:00", "14:00")));
        system.addCourse(new Course("NET101", "Computer Networks",
                3, 25, new TimeSlot("MWF", "14:00", "15:00")));
        system.addCourse(new Course("DB101", "Database Systems",
                3, 25, new TimeSlot("TTh", "14:00", "15:30")));
        system.addCourse(new Course("SE101", "Software Engineering",
                3, 30, new TimeSlot("MWF", "15:00", "16:00")));

        // Set prerequisites
        system.getCourse("CS201").setPrerequisites(List.of("CS101"));
        system.getCourse("CS301").setPrerequisites(List.of("CS201"));
        system.getCourse("CS401").setPrerequisites(List.of("CS301"));

        // --- Sample student accounts ---
        Student alice = new Student("STU001", "Alice Johnson", "Computer Science");
        alice.getCompletedCourses().add("CS101"); // Has completed CS101
        system.addStudent(alice);

        Student bob = new Student("STU002", "Bob Smith", "Mathematics");
        system.addStudent(bob);

        Student carol = new Student("STU003", "Carol Williams", "Information Technology");
        carol.getCompletedCourses().add("CS101");
        carol.getCompletedCourses().add("CS201");
        system.addStudent(carol);
    }

    /** Returns true when data files already exist on disk. */
    public boolean dataFilesExist() {
        return Files.exists(Paths.get(STUDENTS_FILE)) && Files.exists(Paths.get(COURSES_FILE));
    }
}
