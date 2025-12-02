import java.io.*;
import java.util.*;

// --- 1. Custom Exception and Utility Classes (util equivalent) ---

/**
 * Custom exception for when a student record is not found (Lab 3).
 */
class StudentNotFoundException extends Exception {
    public StudentNotFoundException(int rollNo) {
        super("Error: Student with Roll No. " + rollNo + " not found.");
    }
}

/**
 * Simulates a loading process using multithreading (Lab 3).
 * Implements Runnable to be executed by a Thread.
 */
class Loader implements Runnable {
    private final String operation;

    public Loader(String operation) {
        this.operation = operation;
    }

    @Override
    public void run() {
        System.out.print(operation + ".....");
        try {
            // Simulate a delay for responsiveness (Lab 3)
            for (int i = 0; i < 3; i++) {
                Thread.sleep(300);
                System.out.print(".");
            }
            System.out.println();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Loading interrupted.");
        }
    }
}

/**
 * Handles persistent storage of student records using a file (Lab 4).
 * Uses BufferedReader and BufferedWriter.
 */
class FileUtil {

    private static final String FILE_NAME = "students.txt";

    /**
     * Reads student records from the file (Lab 4).
     */
    public static Map<Integer, Student> loadStudents() {
        Map<Integer, Student> studentMap = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // rollNo,name,email,course,marks
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    try {
                        // Use Wrapper Classes (Lab 3)
                        Integer rollNo = Integer.valueOf(parts[0].trim());
                        String name = parts[1].trim();
                        String email = parts[2].trim();
                        String course = parts[3].trim();
                        Double marks = Double.valueOf(parts[4].trim());

                        Student s = new Student(rollNo, name, email, course, marks);
                        studentMap.put(rollNo, s);
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping malformed record: " + line);
                    }
                }
            }
            System.out.println("\nSuccessfully loaded " + studentMap.size() + " student records from " + FILE_NAME);
        } catch (FileNotFoundException e) {
            System.out.println("Data file not found. Starting with an empty student list.");
        } catch (IOException e) {
            System.err.println("Error reading student records from file: " + e.getMessage());
        }
        return studentMap;
    }

    /**
     * Writes all student records to the file (Lab 4).
     */
    public static void saveStudents(Map<Integer, Student> students) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Student student : students.values()) {
                String line = String.format("%d,%s,%s,%s,%.1f%n",
                        student.getRollNo(),
                        student.getName(),
                        student.getEmail(),
                        student.getCourse(),
                        student.getMarks());
                writer.write(line);
            }
            System.out.println("\nSuccessfully saved " + students.size() + " records to " + FILE_NAME);
        } catch (IOException e) {
            System.err.println("Error saving student records to file: " + e.getMessage());
        }
    }
}

// --- 2. Model Classes (model equivalent) ---

/**
 * Abstract class for inheritance (Lab 2).
 */
abstract class Person {
    private String name;
    private String email;

    public Person(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public abstract void displayInfo();

    public String getName() { return name; }
    public String getEmail() { return email; }
}

/**
 * Concrete class implementing core student details (Lab 1) and extending Person (Lab 2).
 * Uses Wrapper Classes (Lab 3).
 */
class Student extends Person {
    private Integer rollNo;
    private String course;
    private Double marks;
    private Character grade;

    public Student(Integer rollNo, String name, String email, String course, Double marks) {
        super(name, email);
        this.rollNo = rollNo;
        this.course = course;
        this.marks = marks;
        calculateGrade();
    }

    /**
     * Calculates the grade based on marks (Lab 1).
     */
    public void calculateGrade() {
        if (marks == null) {
            this.grade = 'I';
        } else if (marks >= 90) {
            this.grade = 'A';
        } else if (marks >= 80) {
            this.grade = 'B';
        } else if (marks >= 70) {
            this.grade = 'C';
        } else {
            this.grade = 'D';
        }
    }

    /**
     * Overrides the abstract method from Person (Lab 2).
     */
    @Override
    public void displayInfo() {
        System.out.println("Roll No: " + this.rollNo);
        System.out.println("Name: " + getName());
        System.out.println("Email: " + getEmail());
        System.out.println("Course: " + this.course);
        System.out.println("Marks: " + this.marks);
        System.out.println("Grade: " + this.grade);
    }

    public Integer getRollNo() { return rollNo; }
    public String getCourse() { return course; }
    public Double getMarks() { return marks; }

    // Comparator for sorting by Marks (Lab 4)
    public static Comparator<Student> MarksComparator = (s1, s2) -> {
        if (s1.getMarks() == null && s2.getMarks() == null) return 0;
        if (s1.getMarks() == null) return -1;
        if (s2.getMarks() == null) return 1;
        // Compare in descending order of marks
        return s2.getMarks().compareTo(s1.getMarks());
    };
}

// --- 3. Service Interface (service equivalent) ---

/**
 * Interface defining CRUD operations (Lab 2).
 */
interface RecordActions {
    void addStudent(Student s);
    void deleteStudent(int rollNo) throws StudentNotFoundException;
    void searchStudent(int rollNo) throws StudentNotFoundException;
    List<Student> viewAllStudents();
    void sortStudentsByMarks();
    void saveAndExit();
}

// --- 4. Service Implementation (service equivalent) ---

/**
 * Manages student operations, implements the interface, and handles business logic.
 * Integrates Collections, Exceptions, Multithreading, and File Handling (Lab 2, 3, 4).
 */
class StudentManager implements RecordActions {

    private final Map<Integer, Student> studentMap;
    private final Scanner scanner;

    public StudentManager() {
        this.studentMap = FileUtil.loadStudents(); // Load data on startup (Lab 4)
        this.scanner = new Scanner(System.in);
    }

    // --- Core Operations (Implementation of RecordActions) ---

    @Override
    public void addStudent(Student s) {
        if (studentMap.containsKey(s.getRollNo())) {
            System.out.println("Error: Student with Roll No. " + s.getRollNo() + " already exists.");
            return;
        }

        // 1. Start Loading Thread (Lab 3)
        Thread loaderThread = new Thread(new Loader("Loading data"));
        loaderThread.start();

        try {
            loaderThread.join(); // Wait for the loading to finish
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 2. Add to Map
        studentMap.put(s.getRollNo(), s);
        System.out.println("Student added successfully.");
        s.displayInfo();
        System.out.println("Program execution completed.");
    }

    @Override
    public void searchStudent(int rollNo) throws StudentNotFoundException {
        Student s = studentMap.get(rollNo);
        if (s == null) {
            throw new StudentNotFoundException(rollNo); // Custom Exception (Lab 3)
        }
        System.out.println("--- Student Found ---");
        s.displayInfo();
        System.out.println("---------------------");
    }

    @Override
    public List<Student> viewAllStudents() {
        return new ArrayList<>(studentMap.values());
    }

    @Override
    public void sortStudentsByMarks() {
        List<Student> students = new ArrayList<>(studentMap.values());
        // Sort using the Comparator (Lab 4)
        students.sort(Student.MarksComparator);

        System.out.println("\nSorted Student List by Marks:");
        // Display using Iterator (Lab 4)
        Iterator<Student> iterator = students.iterator();
        while (iterator.hasNext()) {
            iterator.next().displayInfo();
            System.out.println("---");
        }
    }

    @Override
    public void deleteStudent(int rollNo) throws StudentNotFoundException {
        if (studentMap.remove(rollNo) == null) {
            throw new StudentNotFoundException(rollNo);
        }
        System.out.println("Student with Roll No. " + rollNo + " deleted successfully.");
    }

    @Override
    public void saveAndExit() {
        // 1. Start Loading/Saving Thread (Lab 3)
        Thread loaderThread = new Thread(new Loader("Saving and exiting"));
        loaderThread.start();

        try {
            loaderThread.join(); // Wait for the saving simulation to finish
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 2. Save data to file (Lab 4)
        FileUtil.saveStudents(studentMap);
        System.out.println("Exiting application. Goodbye!");
        scanner.close();
    }

    // --- Input and Validation Method ---

    public void inputAndAddStudent() {
        System.out.println("--- Add New Student ---");

        try {
            System.out.print("Enter Roll No (Integer): ");
            // Autoboxing: int to Integer wrapper (Lab 3)
            Integer rollNo = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter Name: ");
            String name = scanner.nextLine().trim();

            System.out.print("Enter Email: ");
            String email = scanner.nextLine().trim();

            System.out.print("Enter Course: ");
            String course = scanner.nextLine().trim();

            System.out.print("Enter Marks (Double, 0-100): ");
            // Autoboxing: double to Double wrapper (Lab 3)
            Double marks = Double.parseDouble(scanner.nextLine().trim());

            // --- Input Validation (Lab 3) ---
            if (name.isEmpty() || course.isEmpty()) {
                System.out.println("\nError: Name or course cannot be empty.");
                return;
            }

            if (marks < 0 || marks > 100) {
                System.out.println("\nError: Invalid input. Marks must be between 0 and 100.");
                return;
            }

            // Create and add the student
            Student newStudent = new Student(rollNo, name, email, course, marks);
            addStudent(newStudent);

        } catch (NumberFormatException e) {
            // Handle invalid rollNo or marks input that can't be parsed
            System.out.println("\nError: Invalid input format for Roll No or Marks. Please enter valid numbers.");
        } catch (Exception e) {
            System.out.println("\nAn unexpected error occurred: " + e.getMessage());
        }
    }
}

// --- 5. Main Entry Point ---

/**
 * Main application class (Lab 1, 2, 3, 4).
 * Provides the menu interaction.
 */
public class StudentManagementSystem {

    public static void main(String[] args) {
        StudentManager manager = new StudentManager();
        Scanner mainScanner = new Scanner(System.in);
        int choice = -1;

        do {
            displayMenu();
            try {
                System.out.print("Enter choice: ");
                choice = Integer.parseInt(mainScanner.nextLine().trim());

                switch (choice) {
                    case 1: // Add Student
                        manager.inputAndAddStudent();
                        break;
                    case 2: // View All Students
                        System.out.println("\n--- All Student Records ---");
                        manager.viewAllStudents().forEach(Student::displayInfo);
                        System.out.println("---------------------------");
                        break;
                    case 3: // Search by Roll No
                        System.out.print("Enter Roll No to search: ");
                        int rollSearch = Integer.parseInt(mainScanner.nextLine().trim());
                        manager.searchStudent(rollSearch);
                        break;
                    case 4: // Sort by Marks
                        manager.sortStudentsByMarks();
                        break;
                    case 5: // Delete by Roll No
                        System.out.print("Enter Roll No to delete: ");
                        int rollDelete = Integer.parseInt(mainScanner.nextLine().trim());
                        manager.deleteStudent(rollDelete);
                        break;
                    case 6: // Save and Exit
                        manager.saveAndExit();
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number for your choice.");
            } catch (StudentNotFoundException e) {
                System.out.println(e.getMessage());
            }

        } while (choice != 6);
    }

    private static void displayMenu() {
        System.out.println("\n===== Student Menu (Combined) =====");
        System.out.println("1. Add Student (OOP, Validation, Threading)");
        System.out.println("2. View All Students");
        System.out.println("3. Search by Roll No (Exception Handling)");
        System.out.println("4. Sort by Marks (Collections, Comparator, Iterator)");
        System.out.println("5. Delete by Roll No");
        System.out.println("6. Save and Exit (File Handling, Persistence)");
        System.out.println("===================================");
    }
}