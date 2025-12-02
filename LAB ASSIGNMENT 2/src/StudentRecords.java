import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

// ==============================================================================
// 1. Interface: RecordActions (Lab 2)
// Defines the methods for CRUD operations.
// ==============================================================================
interface RecordActions {
    void addStudent(Student student);
    void deleteStudent(int rollNo);
    void updateStudent(int rollNo, Scanner scanner);
    Student searchStudent(int rollNo);
    void viewAllStudents();
}

// ==============================================================================
// 2. Abstract Class: Person (Lab 2)
// Base class with common fields (name, email).
// ==============================================================================
abstract class Person {
    protected String name;
    protected String email;

    // Default Constructor (required for Student's default constructor)
    public Person() {
        this.name = "";
        this.email = "";
    }

    // Parameterized Constructor
    public Person(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // Abstract method to force implementation in Student (Polymorphism)
    public abstract void displayInfo();

    // Getters and Setters (needed for update)
    public String getName() { return name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public void setMarks(double marks) { /* Placeholder for polymorphism */ }
    public double getMarks() { return 0.0; } // Placeholder for update check
}

// ==============================================================================
// 3. Concrete Class: Student (Lab 1 & 2)
// Extends Person, adds student-specific fields/methods.
// ==============================================================================
class Student extends Person {
    private int rollNo;
    private String course;
    private double marks;
    private char grade;

    // Default Constructor (Lab 1)
    public Student() {
        super();
    }

    // Parameterized Constructor (Lab 1)
    public Student(int rollNo, String name, String email, String course, double marks) {
        super(name, email); // Calls Person's constructor (Inheritance)
        this.rollNo = rollNo;
        this.course = course;
        this.marks = marks;
        this.calculateGrade();
    }

    // Input details with data validation (Lab 1)
    public void inputDetails(Scanner scanner) {
        System.out.print("Enter Roll No: ");
        this.rollNo = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter Name: ");
        this.name = scanner.nextLine();

        System.out.print("Enter Email: ");
        this.email = scanner.nextLine();

        System.out.print("Enter Course: ");
        this.course = scanner.nextLine();

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
            System.out.println("Student: " + this.name + " (Roll No: " + this.rollNo + ")");
        } else {
            displayInfo();
        }
    }

    // Getters and Setters override/implement Person's (or add specific ones)
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
// 4. StudentManager (Lab 2)
// Implements the RecordActions interface.
// ==============================================================================
class StudentManager implements RecordActions {
    // Using List for ordered storage and Map for efficient lookup (Lab 1 & 2 requirement)
    private final List<Student> studentList = new ArrayList<>();
    private final Map<Integer, Student> studentMap = new HashMap<>();

    // Helper method to check for duplicate roll number (Lab 2 requirement)
    private boolean isRollNoDuplicate(int rollNo) {
        return studentMap.containsKey(rollNo);
    }

    @Override
    public void addStudent(Student student) {
        if (isRollNoDuplicate(student.getRollNo())) {
            System.err.println("Error: Student with Roll No " + student.getRollNo() + " already exists. Cannot add.");
            return;
        }

        studentList.add(student);
        studentMap.put(student.getRollNo(), student);
        System.out.println("Student added successfully!");
    }

    @Override
    public void deleteStudent(int rollNo) {
        if (studentMap.containsKey(rollNo)) {
            Student studentToRemove = studentMap.get(rollNo);

            studentList.remove(studentToRemove);
            studentMap.remove(rollNo);

            System.out.println("Student record for Roll No " + rollNo + " deleted successfully.");
        } else {
            System.err.println("Error: Student with Roll No " + rollNo + " not found.");
        }
    }

    @Override
    public void updateStudent(int rollNo, Scanner scanner) {
        if (!studentMap.containsKey(rollNo)) {
            System.err.println("Error: Student with Roll No " + rollNo + " not found for update.");
            return;
        }

        Student student = studentMap.get(rollNo);
        System.out.println("Updating record for Roll No: " + rollNo);

        System.out.print("Enter new Email (current: " + student.getEmail() + ", leave blank to skip): ");
        String newEmail = scanner.nextLine();
        if (!newEmail.trim().isEmpty()) {
            student.setEmail(newEmail);
        }

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
    public Student searchStudent(int rollNo) {
        return studentMap.get(rollNo);
    }

    @Override
    public void viewAllStudents() {
        if (studentList.isEmpty()) {
            System.out.println("No records to display.");
            return;
        }
        System.out.println("\n===== All Student Records =====");
        for (Student student : studentList) {
            student.displayInfo(); // Calls the overridden method
            System.out.println("-------------------------");
        }
        System.out.println("--- End of Records ---");
    }
}


// ==============================================================================
// 5. Main Application Class (Lab 1)
// Contains the menu logic.
// ==============================================================================
public class StudentRecords {
    private static final Scanner scanner = new Scanner(System.in);
    private static final StudentManager manager = new StudentManager();

    private static void displayMenu() {
        System.out.println("\n===== Student Record Menu (Lab 1 & 2) =====");
        System.out.println("1. Add Student");
        System.out.println("2. Display All Students");
        System.out.println("3. Search by Roll No");
        System.out.println("4. Update by Roll No");
        System.out.println("5. Delete by Roll No");
        System.out.println("6. Exit");
        System.out.println("===========================================");
    }

    public static void main(String[] args) {
        int choice = 0;

        do {
            displayMenu();
            System.out.print("Enter your choice: ");

            try {
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
                System.out.println("Invalid input. Please enter a numerical choice.");
                choice = 0;
            }
        } while (choice != 6);

        scanner.close();
    }

    private static void addStudentHandler() {
        Student newStudent = new Student();
        newStudent.inputDetails(scanner);
        manager.addStudent(newStudent);
    }

    private static void searchStudentHandler() {
        System.out.print("Enter Roll No to search: ");
        try {
            int rollNo = Integer.parseInt(scanner.nextLine());
            Student student = manager.searchStudent(rollNo);
            if (student != null) {
                System.out.println("\n--- Search Result ---");
                student.displayInfo(true); // Demonstrates method overloading
            } else {
                System.err.println("Student with Roll No " + rollNo + " not found.");
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid input. Please enter a numerical Roll No.");
        }
    }

    private static void updateStudentHandler() {
        System.out.print("Enter Roll No to update: ");
        try {
            int rollNo = Integer.parseInt(scanner.nextLine());
            manager.updateStudent(rollNo, scanner);
        } catch (NumberFormatException e) {
            System.err.println("Invalid input. Please enter a numerical Roll No.");
        }
    }

    private static void deleteStudentHandler() {
        System.out.print("Enter Roll No to delete: ");
        try {
            int rollNo = Integer.parseInt(scanner.nextLine());
            manager.deleteStudent(rollNo);
        } catch (NumberFormatException e) {
            System.err.println("Invalid input. Please enter a numerical Roll No.");
        }
    }
}