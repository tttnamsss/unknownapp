package com.enrollment;

import com.enrollment.model.Course;
import com.enrollment.model.Student;
import com.enrollment.model.TimeSlot;
import com.enrollment.service.DataManager;
import com.enrollment.service.EnrollmentSystem;

import java.io.IOException;
import java.util.*;

/**
 * Entry point for the Course Enrollment System command-line application.
 *
 * <p>Program flow:
 * <ol>
 *   <li>Auto-load saved data (or seed defaults on first run)</li>
 *   <li>Login as Student or Admin</li>
 *   <li>Present role-specific menu</li>
 *   <li>Exit and save on request</li>
 * </ol>
 */
public class Main {

    private static final String ADMIN_PASSWORD = "admin123";
    private static final String SEPARATOR = "=".repeat(70);
    private static final String THIN_SEP  = "-".repeat(70);

    private final EnrollmentSystem system = new EnrollmentSystem();
    private final DataManager dataManager = new DataManager();
    private final Scanner scanner;

    public Main(Scanner scanner) {
        this.scanner = scanner;
    }

    // -----------------------------------------------------------------------
    // Startup
    // -----------------------------------------------------------------------

    public void run() {
        printBanner();
        initData();

        boolean quit = false;
        while (!quit) {
            quit = loginMenu();
        }
    }

    private void printBanner() {
        System.out.println(SEPARATOR);
        System.out.println("       COURSE ENROLLMENT SYSTEM");
        System.out.println(SEPARATOR);
    }

    private void initData() {
        if (dataManager.dataFilesExist()) {
            try {
                dataManager.loadData(system);
                System.out.println("[INFO] Data loaded from disk.");
            } catch (IOException e) {
                System.out.println("[WARN] Could not load saved data: " + e.getMessage());
                System.out.println("[INFO] Starting with default data.");
                dataManager.seedDefaultData(system);
            }
        } else {
            System.out.println("[INFO] First run detected — loading default course catalog and sample students.");
            dataManager.seedDefaultData(system);
        }
    }

    // -----------------------------------------------------------------------
    // Login menu
    // -----------------------------------------------------------------------

    /**
     * Displays the login menu.
     *
     * @return true when the user chooses to exit the application.
     */
    private boolean loginMenu() {
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("  LOGIN");
        System.out.println(SEPARATOR);
        System.out.println("  [1] Login as Student");
        System.out.println("  [2] Login as Admin");
        System.out.println("  [3] Exit");
        System.out.println(THIN_SEP);
        System.out.print("  Select option: ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1" -> studentLogin();
            case "2" -> adminLogin();
            case "3" -> {
                saveAndExit();
                return true;
            }
            default -> System.out.println("  [!] Invalid option. Please enter 1, 2, or 3.");
        }
        return false;
    }

    private void studentLogin() {
        System.out.println();
        System.out.println("  --- Student Login ---");
        System.out.println("  Enter your Student ID (or 'new' to create a new profile): ");
        System.out.print("  > ");
        String id = scanner.nextLine().trim();

        if (id.equalsIgnoreCase("new")) {
            Student newStudent = createStudentProfile();
            if (newStudent != null) {
                studentMenu(newStudent);
            }
            return;
        }

        Student student = system.getStudent(id);
        if (student == null) {
            System.out.println("  [!] Student ID not found. Type 'new' to create a new profile.");
            return;
        }
        System.out.println("  Welcome, " + student.getName() + "!");
        studentMenu(student);
    }

    private void adminLogin() {
        System.out.println();
        System.out.println("  --- Admin Login ---");
        System.out.print("  Password: ");
        String pwd = scanner.nextLine().trim();
        if (!ADMIN_PASSWORD.equals(pwd)) {
            System.out.println("  [!] Incorrect password.");
            return;
        }
        System.out.println("  Welcome, Administrator!");
        adminMenu();
    }

    // -----------------------------------------------------------------------
    // Student menu
    // -----------------------------------------------------------------------

    private void studentMenu(Student student) {
        boolean logout = false;
        while (!logout) {
            System.out.println();
            System.out.println(SEPARATOR);
            System.out.println("  STUDENT MENU  –  " + student.getName() + " [" + student.getId() + "]");
            System.out.println(SEPARATOR);
            System.out.println("  [1] View Course Catalog");
            System.out.println("  [2] Register for a Course");
            System.out.println("  [3] Drop a Course");
            System.out.println("  [4] View My Schedule");
            System.out.println("  [5] Billing Summary");
            System.out.println("  [6] Edit My Profile");
            System.out.println("  [7] Logout and Save");
            System.out.println(THIN_SEP);
            System.out.print("  Select option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> viewCourseCatalog();
                case "2" -> registerForCourse(student);
                case "3" -> dropCourse(student);
                case "4" -> viewSchedule(student);
                case "5" -> billingSummary(student);
                case "6" -> editProfile(student);
                case "7" -> {
                    saveData();
                    logout = true;
                }
                default -> System.out.println("  [!] Invalid option.");
            }
        }
    }

    private void viewCourseCatalog() {
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("  COURSE CATALOG");
        System.out.println(SEPARATOR);
        Collection<Course> courses = system.getAllCourses();
        if (courses.isEmpty()) {
            System.out.println("  No courses available.");
            return;
        }
        printCourseHeader();
        for (Course c : courses) {
            System.out.println("  " + c);
        }
    }

    private void printCourseHeader() {
        System.out.printf("  %-10s %-40s %-8s %-12s %-18s %s%n",
                "Code", "Title", "Credits", "Seats", "Time", "Prerequisites");
        System.out.println("  " + THIN_SEP);
    }

    private void registerForCourse(Student student) {
        System.out.println();
        System.out.println("  --- Register for a Course ---");
        viewCourseCatalog();
        System.out.println();
        System.out.print("  Enter course code to register (or press Enter to cancel): ");
        String code = scanner.nextLine().trim().toUpperCase();
        if (code.isEmpty()) {
            return;
        }
        EnrollmentSystem.EnrollmentResult result = system.registerCourse(student.getId(), code);
        if (result.isSuccess()) {
            System.out.println("  [✓] " + result.getMessage());
        } else {
            System.out.println("  [✗] " + result.getMessage());
        }
    }

    private void dropCourse(Student student) {
        System.out.println();
        System.out.println("  --- Drop a Course ---");
        List<Course> schedule = system.getStudentSchedule(student.getId());
        if (schedule.isEmpty()) {
            System.out.println("  You are not enrolled in any courses.");
            return;
        }
        System.out.println("  Your current courses:");
        for (Course c : schedule) {
            System.out.println("    " + c.getCode() + " – " + c.getTitle());
        }
        System.out.println();
        System.out.print("  Enter course code to drop (or press Enter to cancel): ");
        String code = scanner.nextLine().trim().toUpperCase();
        if (code.isEmpty()) {
            return;
        }
        EnrollmentSystem.EnrollmentResult result = system.dropCourse(student.getId(), code);
        if (result.isSuccess()) {
            System.out.println("  [✓] " + result.getMessage());
        } else {
            System.out.println("  [✗] " + result.getMessage());
        }
    }

    private void viewSchedule(Student student) {
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.printf("  SCHEDULE FOR: %s [%s]%n", student.getName(), student.getId());
        System.out.println(SEPARATOR);
        List<Course> schedule = system.getStudentSchedule(student.getId());
        if (schedule.isEmpty()) {
            System.out.println("  You are not enrolled in any courses.");
            return;
        }
        printCourseHeader();
        for (Course c : schedule) {
            System.out.println("  " + c);
        }
        int totalCredits = schedule.stream().mapToInt(Course::getCredits).sum();
        System.out.println();
        System.out.printf("  Total Credits Enrolled: %d%n", totalCredits);
    }

    private void billingSummary(Student student) {
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.printf("  BILLING SUMMARY FOR: %s [%s]%n", student.getName(), student.getId());
        System.out.println(SEPARATOR);
        List<Course> schedule = system.getStudentSchedule(student.getId());
        if (schedule.isEmpty()) {
            System.out.println("  You are not enrolled in any courses. Tuition: $0.00");
            return;
        }
        System.out.printf("  %-10s %-40s %s%n", "Code", "Title", "Credits");
        System.out.println("  " + THIN_SEP);
        int totalCredits = 0;
        for (Course c : schedule) {
            System.out.printf("  %-10s %-40s %d%n", c.getCode(), c.getTitle(), c.getCredits());
            totalCredits += c.getCredits();
        }
        double tuition = system.calculateTuition(student.getId());
        System.out.println("  " + THIN_SEP);
        System.out.printf("  Total Credits : %d%n", totalCredits);
        System.out.printf("  Rate per Credit: $300.00%n");
        System.out.printf("  %-42s $%.2f%n", "TOTAL TUITION:", tuition);
    }

    private void editProfile(Student student) {
        System.out.println();
        System.out.println("  --- Edit My Profile ---");
        System.out.println("  Current: " + student);
        System.out.println("  (Press Enter to keep current value)");
        System.out.print("  New Name  [" + student.getName() + "]: ");
        String name = scanner.nextLine().trim();
        System.out.print("  New Major [" + student.getMajor() + "]: ");
        String major = scanner.nextLine().trim();
        system.updateStudent(student.getId(), name, major);
        System.out.println("  [✓] Profile updated.");
        System.out.println("  Updated: " + student);
    }

    private Student createStudentProfile() {
        System.out.println();
        System.out.println("  --- Create New Student Profile ---");
        System.out.print("  Student ID: ");
        String id = scanner.nextLine().trim();
        if (id.isEmpty()) {
            System.out.println("  [!] Student ID cannot be empty.");
            return null;
        }
        if (system.getStudent(id) != null) {
            System.out.println("  [!] Student ID already exists.");
            return null;
        }
        System.out.print("  Full Name : ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("  [!] Name cannot be empty.");
            return null;
        }
        System.out.print("  Major     : ");
        String major = scanner.nextLine().trim();
        if (major.isEmpty()) {
            major = "Undeclared";
        }
        Student student = new Student(id, name, major);
        system.addStudent(student);
        System.out.println("  [✓] New student profile created: " + student);
        return student;
    }

    // -----------------------------------------------------------------------
    // Admin menu
    // -----------------------------------------------------------------------

    private void adminMenu() {
        boolean logout = false;
        while (!logout) {
            System.out.println();
            System.out.println(SEPARATOR);
            System.out.println("  ADMIN MENU");
            System.out.println(SEPARATOR);
            System.out.println("  [1]  View Course Catalog");
            System.out.println("  [2]  View Class Roster");
            System.out.println("  [3]  View All Students");
            System.out.println("  [4]  Add New Student");
            System.out.println("  [5]  Edit Student Profile");
            System.out.println("  [6]  Add New Course");
            System.out.println("  [7]  Edit Course");
            System.out.println("  [8]  View Student Schedule");
            System.out.println("  [9]  Billing Summary (any student)");
            System.out.println("  [10] Logout and Save");
            System.out.println(THIN_SEP);
            System.out.print("  Select option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1"  -> viewCourseCatalog();
                case "2"  -> adminViewRoster();
                case "3"  -> adminViewStudents();
                case "4"  -> adminAddStudent();
                case "5"  -> adminEditStudent();
                case "6"  -> adminAddCourse();
                case "7"  -> adminEditCourse();
                case "8"  -> adminViewSchedule();
                case "9"  -> adminBillingSummary();
                case "10" -> {
                    saveData();
                    logout = true;
                }
                default -> System.out.println("  [!] Invalid option.");
            }
        }
    }

    private void adminViewRoster() {
        System.out.println();
        System.out.println("  --- Class Roster ---");
        viewCourseCatalog();
        System.out.println();
        System.out.print("  Enter course code (or press Enter to cancel): ");
        String code = scanner.nextLine().trim().toUpperCase();
        if (code.isEmpty()) {
            return;
        }
        Course course = system.getCourse(code);
        if (course == null) {
            System.out.println("  [!] Course not found: " + code);
            return;
        }
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.printf("  ROSTER: %s – %s%n", course.getCode(), course.getTitle());
        System.out.println(SEPARATOR);
        List<Student> roster = system.getCourseRoster(code);
        if (roster.isEmpty()) {
            System.out.println("  No students enrolled.");
            return;
        }
        System.out.printf("  %-15s %-25s %s%n", "ID", "Name", "Major");
        System.out.println("  " + THIN_SEP);
        for (Student s : roster) {
            System.out.printf("  %-15s %-25s %s%n", s.getId(), s.getName(), s.getMajor());
        }
        System.out.printf("  %nTotal enrolled: %d / %d%n", roster.size(), course.getCapacity());
    }

    private void adminViewStudents() {
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("  ALL STUDENTS");
        System.out.println(SEPARATOR);
        Collection<Student> students = system.getAllStudents();
        if (students.isEmpty()) {
            System.out.println("  No students registered.");
            return;
        }
        System.out.printf("  %-15s %-25s %-20s %s%n", "ID", "Name", "Major", "Enrolled Courses");
        System.out.println("  " + THIN_SEP);
        for (Student s : students) {
            String enrolled = s.getEnrolledCourses().isEmpty() ? "None"
                    : String.join(", ", s.getEnrolledCourses());
            System.out.printf("  %-15s %-25s %-20s %s%n",
                    s.getId(), s.getName(), s.getMajor(), enrolled);
        }
    }

    private void adminAddStudent() {
        System.out.println();
        System.out.println("  --- Add New Student ---");
        System.out.print("  Student ID: ");
        String id = scanner.nextLine().trim();
        if (id.isEmpty()) {
            System.out.println("  [!] Student ID cannot be empty.");
            return;
        }
        if (system.getStudent(id) != null) {
            System.out.println("  [!] Student ID already exists.");
            return;
        }
        System.out.print("  Full Name : ");
        String name = scanner.nextLine().trim();
        System.out.print("  Major     : ");
        String major = scanner.nextLine().trim();
        if (major.isEmpty()) {
            major = "Undeclared";
        }
        system.addStudent(new Student(id, name, major));
        System.out.println("  [✓] Student added.");
    }

    private void adminEditStudent() {
        System.out.println();
        System.out.println("  --- Edit Student Profile ---");
        adminViewStudents();
        System.out.println();
        System.out.print("  Enter Student ID to edit (or press Enter to cancel): ");
        String id = scanner.nextLine().trim();
        if (id.isEmpty()) {
            return;
        }
        Student student = system.getStudent(id);
        if (student == null) {
            System.out.println("  [!] Student not found: " + id);
            return;
        }
        System.out.println("  Current: " + student);
        System.out.println("  (Press Enter to keep current value)");
        System.out.print("  New Name  [" + student.getName() + "]: ");
        String name = scanner.nextLine().trim();
        System.out.print("  New Major [" + student.getMajor() + "]: ");
        String major = scanner.nextLine().trim();
        system.updateStudent(id, name, major);
        System.out.println("  [✓] Profile updated: " + student);
    }

    private void adminAddCourse() {
        System.out.println();
        System.out.println("  --- Add New Course ---");
        System.out.print("  Course Code    : ");
        String code = scanner.nextLine().trim().toUpperCase();
        if (code.isEmpty()) {
            System.out.println("  [!] Course code cannot be empty.");
            return;
        }
        if (system.getCourse(code) != null) {
            System.out.println("  [!] Course code already exists: " + code);
            return;
        }
        System.out.print("  Title          : ");
        String title = scanner.nextLine().trim();
        System.out.print("  Credits        : ");
        int credits = parseIntOrDefault(scanner.nextLine().trim(), 3);
        System.out.print("  Capacity       : ");
        int capacity = parseIntOrDefault(scanner.nextLine().trim(), 30);
        System.out.print("  Days (e.g. MWF): ");
        String days = scanner.nextLine().trim();
        System.out.print("  Start Time (HH:mm): ");
        String start = scanner.nextLine().trim();
        System.out.print("  End Time   (HH:mm): ");
        String end = scanner.nextLine().trim();
        System.out.print("  Prerequisites (comma-separated codes, or blank): ");
        String prereqInput = scanner.nextLine().trim();

        TimeSlot timeSlot = new TimeSlot(days, start, end);
        Course course = new Course(code, title, credits, capacity, timeSlot);
        if (!prereqInput.isEmpty()) {
            List<String> prereqs = Arrays.asList(prereqInput.split("\\s*,\\s*"));
            course.setPrerequisites(prereqs);
        }
        system.addCourse(course);
        System.out.println("  [✓] Course added: " + course);
    }

    private void adminEditCourse() {
        System.out.println();
        System.out.println("  --- Edit Course ---");
        viewCourseCatalog();
        System.out.println();
        System.out.print("  Enter course code to edit (or press Enter to cancel): ");
        String code = scanner.nextLine().trim().toUpperCase();
        if (code.isEmpty()) {
            return;
        }
        Course course = system.getCourse(code);
        if (course == null) {
            System.out.println("  [!] Course not found: " + code);
            return;
        }
        System.out.println("  Current: " + course);
        System.out.println("  (Press Enter to keep current value)");
        System.out.print("  New Title    [" + course.getTitle() + "]: ");
        String title = scanner.nextLine().trim();
        System.out.print("  New Credits  [" + course.getCredits() + "]: ");
        String creditsStr = scanner.nextLine().trim();
        System.out.print("  New Capacity [" + course.getCapacity() + "]: ");
        String capStr = scanner.nextLine().trim();

        Integer credits = creditsStr.isEmpty() ? null : parseIntOrNull(creditsStr);
        Integer capacity = capStr.isEmpty() ? null : parseIntOrNull(capStr);
        system.updateCourse(code, title.isEmpty() ? null : title, credits, capacity);
        System.out.println("  [✓] Course updated: " + course);
    }

    private void adminViewSchedule() {
        System.out.println();
        System.out.println("  --- View Student Schedule ---");
        adminViewStudents();
        System.out.println();
        System.out.print("  Enter Student ID (or press Enter to cancel): ");
        String id = scanner.nextLine().trim();
        if (id.isEmpty()) {
            return;
        }
        Student student = system.getStudent(id);
        if (student == null) {
            System.out.println("  [!] Student not found: " + id);
            return;
        }
        viewSchedule(student);
    }

    private void adminBillingSummary() {
        System.out.println();
        System.out.println("  --- Billing Summary ---");
        adminViewStudents();
        System.out.println();
        System.out.print("  Enter Student ID (or press Enter to cancel): ");
        String id = scanner.nextLine().trim();
        if (id.isEmpty()) {
            return;
        }
        Student student = system.getStudent(id);
        if (student == null) {
            System.out.println("  [!] Student not found: " + id);
            return;
        }
        billingSummary(student);
    }

    // -----------------------------------------------------------------------
    // Persistence helpers
    // -----------------------------------------------------------------------

    private void saveData() {
        try {
            dataManager.saveData(system);
            System.out.println("  [✓] Data saved successfully.");
        } catch (IOException e) {
            System.out.println("  [!] Error saving data: " + e.getMessage());
        }
    }

    private void saveAndExit() {
        saveData();
        System.out.println();
        System.out.println("  Thank you for using the Course Enrollment System. Goodbye!");
        System.out.println(SEPARATOR);
    }

    // -----------------------------------------------------------------------
    // Utility helpers
    // -----------------------------------------------------------------------

    private int parseIntOrDefault(String s, int defaultValue) {
        try {
            int v = Integer.parseInt(s);
            return v > 0 ? v : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Integer parseIntOrNull(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // -----------------------------------------------------------------------
    // Main entry point
    // -----------------------------------------------------------------------

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            new Main(scanner).run();
        }
    }
}
