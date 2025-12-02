import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

// ==============================================================================
// 1. Custom Exception (Lab 3)
// Handles cases where a requested student record is not found.
// ==============================================================================
class StudentNotFoundException extends Exception {
    public StudentNotFoundException(String message) {
        // Calls the constructor of the parent class (Exception)
        super(message);
    }
}

// ==============================================================================
// 2. Multithreading Loader (Lab 3)
// Implements Runnable to simulate a loading delay when adding a record.
// ==============================================================================
class Loader implements Runnable {
    @Override
    public void run() {
        try {
            System.out.print("Simulating loading process.");
            for (int i = 0; i < 3; i++) {
                Thread.sleep(400);
                System.out.print(".");
            }
            System.out.println(" Done!");
        } catch (InterruptedException e) {
            // Proper handling for thread interruption
            Thread.currentThread().interrupt();
        }
    }
}

// ==============================================================================
// 3. Interface: RecordActions (Lab 2)
// Defines the contract for student management operations (CRUD).
// ==============================================================================
interface RecordActions {
    // Throws a general Exception for duplicate roll number validation (Lab 3)
    void addStudent(Student student) throws Exception;

    // Throws custom exception (Lab 3)
    void deleteStudent(int rollNo) throws StudentNotFoundException;

    // Throws custom exception (Lab 3)
    void updateStudent(int rollNo, Scanner scanner) throws StudentNotFoundException;

    // Throws custom exception (Lab 3)
    Student searchStudent(int rollNo) throws StudentNotFoundException;

    void viewAllStudents();
}

// ==============================================================================
// 4. Abstract Class: Person (Lab 2)
// Base class for inheritance.
// ==============================================================================
abstract class Person {
    protected String name;
    protected String email;

    public Person(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // Default Constructor (required for Student's default constructor)
    public Person() {}

    // Abstract method (requires implementation in Student)
    public abstract void displayInfo();

    // Getters and Setters
    public String getName() { return name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    // Add placeholders to allow StudentManager to use them
    public int getRollNo() { return 0; }
    public double getMarks() { return 0.0; }
    public void setMarks(double marks) {}
}

// ==============================================================================
// 5. Concrete Class: Student (Lab 1 & 2)
// Extends Person and adds student-specific functionality.
// ==============================================================================
class Student extends Person {
    private int rollNo;
    private String course;
    private double marks;
    private char grade;

    // Parameterized Constructor
    public Student(int rollNo, String name, String email, String course, double marks) {
        super(name, email); // Inheritance usage
        this.rollNo = rollNo;
        this.course = course;
        this.marks = marks;
        this.calculateGrade();
    }

    // Default Constructor (Lab 1)
    public Student() {
        super();
    }

    // Method to take input details with validation (Lab 1 & 3)
    public void inputDetails(Scanner scanner) {
        // Use Wrapper Classes (Integer/Double) and parsing (Lab 3)
        System.out.print("Enter Roll No: ");
        this.rollNo = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter Name: ");
        this.name = scanner.nextLine();

        System.out.print("Enter Email: ");
        this.email = scanner.nextLine();

        System.out.print("Enter Course: ");
        this.course = scanner.nextLine();

        // Data Validation (Lab 1) and Exception Handling (Lab 3) for marks
        boolean validMarks = false;
        while (!validMarks) {
            System.out.print("Enter Marks (0-100): ");
            try {
                this.marks = Double.parseDouble(scanner.nextLine());
                if (this.marks >= 0 && this.marks <= 100) {
                    validMarks = true;
                } else {
                    System.out.println("Marks must be between 0 and 100. Please re-enter.");
                }
            } catch (NumberFormatException e) {
                // Catches non-numeric input
                System.out.println("Invalid input. Please enter a numerical value for marks.");
            }
        }
        this.calculateGrade();
    }

    // Calculate Grade (Lab 1)
    public void calculateGrade() {
        if (marks >= 90) this.grade = 'A';
        else if (marks >= 80) this.grade = 'B';
        else if (marks >= 70) this.grade = 'C';
        else this.grade = 'D';
    }

    // Method Overriding: Implements abstract displayInfo() (Lab 2, Dynamic Polymorphism)
    @Override
    public void displayInfo() {
        System.out.println("Roll No: " + this.rollNo);
        System.out.println("Name: " + this.name);
        System.out.println("Email: " + this.email);
        System.out.println("Course: " + this.course);
        System.out.println("Marks: " + this.marks);
        System.out.println("Grade: " + this.grade);
    }

    // Method Overloading: Provides a simpler display (Lab 2, Static Polymorphism)
    public void displayInfo(boolean simpleFormat) {
        if (simpleFormat) {
            System.out.println("Roll No: " + this.rollNo + " | Name: " + this.name + " | Grade: " + this.grade);
        } else {
            displayInfo();
        }
    }

    // Getters/Setters overriding/implementing Person's methods
    @Override
    public int getRollNo() { return rollNo; }
    @Override
    public double getMarks() { return marks; }
    @Override
    public void setMarks(double marks) {
        this.marks = marks;
        this.calculateGrade();
    }
}

// ==============================================================================
// 6. StudentManager (Lab 2 & 3)
// Implements the interface, handles collections, duplicates, and multithreading.
// ==============================================================================
class StudentManager implements RecordActions {
    // Using List for ordered storage and Map for efficient lookup (Lab 2)
    private final List<Student> studentList = new ArrayList<>();
    private final Map<Integer, Student> studentMap = new HashMap<>();

    private boolean isRollNoDuplicate(int rollNo) {
        return studentMap.containsKey(rollNo);
    }

    @Override
    public void addStudent(Student student) throws Exception {
        if (isRollNoDuplicate(student.getRollNo())) {
            // Throw a general Exception for duplicate roll number (Lab 3)
            throw new Exception("Error: Student with Roll No " + student.getRollNo() + " already exists.");
        }

        // Multithreading simulation when adding (Lab 3)
        Thread loaderThread = new Thread(new Loader());
        loaderThread.start();
        try {
            loaderThread.join(); // Wait for the loading to finish
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        studentList.add(student);
        studentMap.put(student.getRollNo(), student);
        System.out.println("Student added successfully!");
    }

    @Override
    public void deleteStudent(int rollNo) throws StudentNotFoundException {
        if (!studentMap.containsKey(rollNo)) {
            // Throw custom exception (Lab 3)
            throw new StudentNotFoundException("Student with Roll No " + rollNo + " not found for deletion.");
        }

        Student studentToRemove = studentMap.get(rollNo);
        studentList.remove(studentToRemove);
        studentMap.remove(rollNo);

        System.out.println("Student record for Roll No " + rollNo + " deleted successfully.");
    }

    @Override
    public void updateStudent(int rollNo, Scanner scanner) throws StudentNotFoundException {
        if (!studentMap.containsKey(rollNo)) {
            // Throw custom exception (Lab 3)
            throw new StudentNotFoundException("Student with Roll No " + rollNo + " not found for update.");
        }

        Student student = studentMap.get(rollNo);
        System.out.println("Updating record for Roll No: " + rollNo);

        System.out.print("Enter new Email (current: " + student.getEmail() + ", leave blank to skip): ");
        String newEmail = scanner.nextLine();
        if (!newEmail.trim().isEmpty()) {
            student.setEmail(newEmail);
        }

        // Exception Handling for update input (Lab 3)
        System.out.print("Enter new Marks (current: " + student.getMarks() + ", leave blank to skip): ");
        String marksInput = scanner.nextLine();
        if (!marksInput.trim().isEmpty()) {
            try {
                double newMarks = Double.parseDouble(marksInput);
                if (newMarks >= 0 && newMarks <= 100) {
                    student.setMarks(newMarks);
                    System.out.println("Marks and Grade updated.");
                } else {
                    System.out.println("Update failed: Marks must be between 0 and 100.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Update failed: Invalid number format for marks.");
            }
        }
        System.out.println("Update complete.");
    }

    @Override
    public Student searchStudent(int rollNo) throws StudentNotFoundException {
        Student student = studentMap.get(rollNo);
        if (student == null) {
            // Throw custom exception (Lab 3)
            throw new StudentNotFoundException("Student with Roll No " + rollNo + " not found.");
        }
        return student;
    }

    @Override
    public void viewAllStudents() {
        if (studentList.isEmpty()) {
            System.out.println("No records to display.");
            return;
        }
        System.out.println("\n===== All Student Records =====");
        for (Student student : studentList) {
            student.displayInfo(); // Dynamic Polymorphism
            System.out.println("-------------------------");
        }
        System.out.println("--- End of Records ---");
    }
}


// ==============================================================================
// 7. Main Application Class (Lab 1, 2, 3)
// Contains the menu logic and main exception handling.
// ==============================================================================
public class StudentRecordsComplete {
    private static final Scanner scanner = new Scanner(System.in);
    private static final StudentManager manager = new StudentManager();

    private static void displayMenu() {
        System.out.println("\n===== Student Record Menu (Lab 1, 2, 3) =====");
        System.out.println("1. Add Student");
        System.out.println("2. Display All Students");
        System.out.println("3. Search by Roll No");
        System.out.println("4. Update by Roll No");
        System.out.println("5. Delete by Roll No");
        System.out.println("6. Exit");
        System.out.println("=============================================");
    }

    public static void main(String[] args) {
        int choice = 0;

        do {
            displayMenu();
            System.out.print("Enter your choice: ");

            try {
                // Wrapper Class (Integer) and Exception Handling (Lab 3)
                String input = scanner.nextLine();
                if (input.trim().isEmpty()) {
                    choice = 0;
                    continue;
                }
                choice = Integer.parseInt(input);

                switch (choice) {
                    case 1:
                        addStudentHandler();
                        break;
                    case 2:
                        manager.viewAllStudents();
                        break;
                    case 3:
                        searchStudentHandler();
                        break;
                    case 4:
                        updateStudentHandler();
                        break;
                    case 5:
                        deleteStudentHandler();
                        break;
                    case 6:
                        System.out.println("Exiting the application. Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter a number between 1 and 6.");
                }
            } catch (NumberFormatException e) {
                // General exception handling for non-numeric menu input (Lab 3)
                System.out.println("Invalid input. Please enter a numerical choice.");
                choice = 0;
            } catch (Exception e) {
                // Catch-all for exceptions thrown by handlers (e.g., duplicate roll no)
                System.err.println(e.getMessage());
                choice = 0;
            }
        } while (choice != 6);

        // Final block in try-catch-finally is implicitly handled by the loop structure and resource closing.
        scanner.close();
    }

    private static void addStudentHandler() throws Exception {
        try {
            Student newStudent = new Student();
            newStudent.inputDetails(scanner);
            manager.addStudent(newStudent);
        } catch (NumberFormatException e) {
            // Re-throw or handle parsing error in input details
            throw new Exception("Input error: Roll No must be a number.");
        }
    }

    private static void searchStudentHandler() {
        System.out.print("Enter Roll No to search: ");
        try {
            int rollNo = Integer.parseInt(scanner.nextLine());
            Student student = manager.searchStudent(rollNo);

            System.out.println("\n--- Search Result ---");
            student.displayInfo(true); // Demonstrates Method Overloading
            System.out.println("--- Full Info ---");
            student.displayInfo(false);
        } catch (NumberFormatException e) {
            System.err.println("Invalid input. Please enter a numerical Roll No.");
        } catch (StudentNotFoundException e) {
            System.err.println(e.getMessage()); // Handles custom exception (Lab 3)
        }
    }

    private static void updateStudentHandler() {
        System.out.print("Enter Roll No to update: ");
        try {
            int rollNo = Integer.parseInt(scanner.nextLine());
            manager.updateStudent(rollNo, scanner);
        } catch (NumberFormatException e) {
            System.err.println("Invalid input. Please enter a numerical Roll No.");
        } catch (StudentNotFoundException e) {
            System.err.println(e.getMessage()); // Handles custom exception (Lab 3)
        }
    }

    private static void deleteStudentHandler() {
        System.out.print("Enter Roll No to delete: ");
        try {
            int rollNo = Integer.parseInt(scanner.nextLine());
            manager.deleteStudent(rollNo);
        } catch (NumberFormatException e) {
            System.err.println("Invalid input. Please enter a numerical Roll No.");
        } catch (StudentNotFoundException e) {
            System.err.println(e.getMessage()); // Handles custom exception (Lab 3)
        }
    }
}