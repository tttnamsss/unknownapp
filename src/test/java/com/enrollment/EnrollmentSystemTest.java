package com.enrollment;

import com.enrollment.model.Course;
import com.enrollment.model.Student;
import com.enrollment.model.TimeSlot;
import com.enrollment.service.DataManager;
import com.enrollment.service.EnrollmentSystem;
import com.enrollment.service.EnrollmentSystem.EnrollmentResult;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Course Enrollment System.
 */
class EnrollmentSystemTest {

    private EnrollmentSystem system;

    @BeforeEach
    void setUp() {
        system = new EnrollmentSystem();

        // Courses
        Course cs101 = new Course("CS101", "Intro to Programming", 3, 30,
                new TimeSlot("MWF", "09:00", "10:00"));
        Course cs201 = new Course("CS201", "Data Structures", 3, 25,
                new TimeSlot("MWF", "10:00", "11:00"));
        cs201.setPrerequisites(List.of("CS101"));
        Course math101 = new Course("MATH101", "Calculus I", 4, 35,
                new TimeSlot("MWF", "09:00", "10:00")); // conflicts with CS101
        Course net101 = new Course("NET101", "Computer Networks", 3, 2,
                new TimeSlot("TTh", "14:00", "15:30")); // small capacity for full-test

        system.addCourse(cs101);
        system.addCourse(cs201);
        system.addCourse(math101);
        system.addCourse(net101);

        // Students
        Student alice = new Student("STU001", "Alice", "CS");
        alice.getCompletedCourses().add("CS101"); // prerequisite already completed
        system.addStudent(alice);

        Student bob = new Student("STU002", "Bob", "Math");
        system.addStudent(bob);
    }

    // -----------------------------------------------------------------------
    // Student management
    // -----------------------------------------------------------------------

    @Test
    void addStudent_success() {
        Student carol = new Student("STU003", "Carol", "IT");
        assertTrue(system.addStudent(carol));
        assertNotNull(system.getStudent("STU003"));
    }

    @Test
    void addStudent_duplicateId_returnsFalse() {
        Student duplicate = new Student("STU001", "Duplicate", "CS");
        assertFalse(system.addStudent(duplicate));
    }

    @Test
    void updateStudent_changesNameAndMajor() {
        assertTrue(system.updateStudent("STU001", "Alice Updated", "Biology"));
        assertEquals("Alice Updated", system.getStudent("STU001").getName());
        assertEquals("Biology", system.getStudent("STU001").getMajor());
    }

    @Test
    void updateStudent_notFound_returnsFalse() {
        assertFalse(system.updateStudent("UNKNOWN", "Name", "Major"));
    }

    // -----------------------------------------------------------------------
    // Course management
    // -----------------------------------------------------------------------

    @Test
    void addCourse_success() {
        Course eng101 = new Course("ENG101", "Technical Writing", 2, 40,
                new TimeSlot("TTh", "13:00", "14:00"));
        assertTrue(system.addCourse(eng101));
        assertNotNull(system.getCourse("ENG101"));
    }

    @Test
    void addCourse_duplicateCode_returnsFalse() {
        Course dup = new Course("CS101", "Duplicate", 3, 20, null);
        assertFalse(system.addCourse(dup));
    }

    @Test
    void courseFull_returnsTrue_whenAtCapacity() {
        Course small = system.getCourse("NET101");
        assertFalse(small.isFull());
        small.enrollStudent("X1");
        small.enrollStudent("X2");
        assertTrue(small.isFull());
    }

    // -----------------------------------------------------------------------
    // Enrollment – happy path
    // -----------------------------------------------------------------------

    @Test
    void registerCourse_successfulEnrollment() {
        EnrollmentResult result = system.registerCourse("STU001", "CS201");
        assertTrue(result.isSuccess(), result.getMessage());
        assertTrue(system.getStudent("STU001").isEnrolledIn("CS201"));
        assertTrue(system.getCourse("CS201").hasStudent("STU001"));
    }

    @Test
    void dropCourse_successfulDrop() {
        system.registerCourse("STU001", "CS201");
        EnrollmentResult result = system.dropCourse("STU001", "CS201");
        assertTrue(result.isSuccess(), result.getMessage());
        assertFalse(system.getStudent("STU001").isEnrolledIn("CS201"));
        assertFalse(system.getCourse("CS201").hasStudent("STU001"));
    }

    // -----------------------------------------------------------------------
    // Enrollment – error cases
    // -----------------------------------------------------------------------

    @Test
    void registerCourse_alreadyEnrolled_returnsFalse() {
        system.registerCourse("STU001", "CS201");
        EnrollmentResult result = system.registerCourse("STU001", "CS201");
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("already enrolled"));
    }

    @Test
    void registerCourse_courseFull_returnsFalse() {
        // Fill NET101 (capacity 2) with other students
        system.addStudent(new Student("S9", "S9", "IT"));
        system.addStudent(new Student("S10", "S10", "IT"));
        system.registerCourse("S9", "NET101");
        system.registerCourse("S10", "NET101");

        EnrollmentResult result = system.registerCourse("STU001", "NET101");
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().toLowerCase().contains("full"));
    }

    @Test
    void registerCourse_prerequisiteNotMet_returnsFalse() {
        // Bob has NOT completed CS101
        EnrollmentResult result = system.registerCourse("STU002", "CS201");
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().toLowerCase().contains("prerequisite"));
    }

    @Test
    void registerCourse_prerequisiteMet_success() {
        // Alice HAS completed CS101
        EnrollmentResult result = system.registerCourse("STU001", "CS201");
        assertTrue(result.isSuccess(), result.getMessage());
    }

    @Test
    void registerCourse_timeConflict_returnsFalse() {
        // CS101 and MATH101 share the same MWF 09:00-10:00 slot
        system.registerCourse("STU001", "CS101");
        EnrollmentResult result = system.registerCourse("STU001", "MATH101");
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().toLowerCase().contains("conflict"));
    }

    @Test
    void dropCourse_notEnrolled_returnsFalse() {
        EnrollmentResult result = system.dropCourse("STU001", "CS101");
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("not enrolled"));
    }

    @Test
    void registerCourse_unknownStudent_returnsFalse() {
        EnrollmentResult result = system.registerCourse("UNKNOWN", "CS101");
        assertFalse(result.isSuccess());
    }

    @Test
    void registerCourse_unknownCourse_returnsFalse() {
        EnrollmentResult result = system.registerCourse("STU001", "UNKNOWN");
        assertFalse(result.isSuccess());
    }

    // -----------------------------------------------------------------------
    // Reporting
    // -----------------------------------------------------------------------

    @Test
    void getStudentSchedule_returnsEnrolledCourses() {
        system.registerCourse("STU001", "CS101");
        system.registerCourse("STU001", "CS201");
        List<Course> schedule = system.getStudentSchedule("STU001");
        assertEquals(2, schedule.size());
    }

    @Test
    void getCourseRoster_returnsEnrolledStudents() {
        system.registerCourse("STU001", "CS101");
        system.registerCourse("STU002", "CS101");
        List<Student> roster = system.getCourseRoster("CS101");
        assertEquals(2, roster.size());
    }

    @Test
    void calculateTuition_correctAmount() {
        system.registerCourse("STU001", "CS101"); // 3 credits
        system.registerCourse("STU001", "CS201"); // 3 credits
        double tuition = system.calculateTuition("STU001");
        assertEquals(1800.0, tuition, 0.001); // 6 credits * $300
    }

    @Test
    void calculateTuition_noEnrollment_returnsZero() {
        double tuition = system.calculateTuition("STU001");
        assertEquals(0.0, tuition, 0.001);
    }

    // -----------------------------------------------------------------------
    // TimeSlot overlap detection
    // -----------------------------------------------------------------------

    @Test
    void timeSlot_overlapping_sameDay_sameTime() {
        TimeSlot a = new TimeSlot("MWF", "09:00", "10:00");
        TimeSlot b = new TimeSlot("MWF", "09:00", "10:00");
        assertTrue(a.overlaps(b));
    }

    @Test
    void timeSlot_overlapping_sameDay_partialOverlap() {
        TimeSlot a = new TimeSlot("MWF", "09:00", "10:30");
        TimeSlot b = new TimeSlot("MWF", "10:00", "11:00");
        assertTrue(a.overlaps(b));
    }

    @Test
    void timeSlot_noOverlap_differentDays() {
        TimeSlot a = new TimeSlot("MWF", "09:00", "10:00");
        TimeSlot b = new TimeSlot("TTh", "09:00", "10:00");
        assertFalse(a.overlaps(b));
    }

    @Test
    void timeSlot_noOverlap_consecutiveTimes() {
        TimeSlot a = new TimeSlot("MWF", "09:00", "10:00");
        TimeSlot b = new TimeSlot("MWF", "10:00", "11:00");
        assertFalse(a.overlaps(b));
    }

    // -----------------------------------------------------------------------
    // Data persistence (DataManager)
    // -----------------------------------------------------------------------

    @Test
    void dataManager_saveAndLoad_roundTrip() throws IOException {
        // Use a temp directory for test data
        Path tempDir = Files.createTempDirectory("enrollment_test_");
        System.setProperty("user.dir", tempDir.toString());

        try {
            // Enroll alice in CS101
            system.registerCourse("STU001", "CS101");

            DataManager dm = new DataManager();
            // We need to save relative to the temp dir – do it via reflection-free approach:
            // Save to temp dir by creating data subdir inside it
            Path dataDir = tempDir.resolve("data");
            Files.createDirectories(dataDir);

            // Use the DataManager save (it uses "data/" relative to CWD)
            Path origCwd = Paths.get(System.getProperty("user.dir"));
            dm.saveData(system);

            // Load into a fresh system
            EnrollmentSystem system2 = new EnrollmentSystem();
            dm.loadData(system2);

            Student loaded = system2.getStudent("STU001");
            assertNotNull(loaded);
            assertEquals("Alice", loaded.getName());
            assertTrue(loaded.isEnrolledIn("CS101"));

            Course loadedCourse = system2.getCourse("CS101");
            assertNotNull(loadedCourse);
            assertEquals("Intro to Programming", loadedCourse.getTitle());
            assertTrue(loadedCourse.hasStudent("STU001"));
        } finally {
            // Cleanup
            deleteDirectory(tempDir);
        }
    }

    private void deleteDirectory(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                 .sorted(Comparator.reverseOrder())
                 .forEach(p -> {
                     try { Files.delete(p); } catch (IOException ignored) {}
                 });
        }
    }
}
