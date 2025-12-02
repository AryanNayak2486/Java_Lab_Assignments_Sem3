import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// The core class definition
class Student {
    // Fields: rollNo (int), name (String), course (String), marks (double), grade (char)
    private int rollNo;
    private String name;
    private String course;
    private double marks;
    private char grade;

    // 1. Default Constructor
    public Student() {
        this.name = "";
        this.course = "";
    }

    // 2. Parameterized Constructor
    public Student(int rollNo, String name, String course, double marks) {
        this.rollNo = rollNo;
        this.name = name;
        this.course = course;
        this.marks = marks;
        this.calculateGrade();
    }

    // Method to take input details from the user
    public void inputDetails(Scanner scanner) {
        System.out.print("Enter Roll No: ");
        // Using parseInt to handle int input
        this.rollNo = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter Name: ");
        this.name = scanner.nextLine();

        System.out.print("Enter Course: ");
        this.course = scanner.nextLine();

        // Data Validation: Ensure marks are between 0 and 100
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

    // Method to calculate the grade (A, B, C, D)
    public void calculateGrade() {
        // Conditional statements (if-else if-else) to control flow
        if (marks >= 90) {
            this.grade = 'A';
        } else if (marks >= 80) {
            this.grade = 'B';
        } else if (marks >= 70) {
            this.grade = 'C';
        } else {
            this.grade = 'D';
        }
    }

    // Method to display student details
    public void displayDetails() {
        System.out.println("Roll No: " + this.rollNo); //
        System.out.println("Name: " + this.name);
        System.out.println("Course: " + this.course);
        System.out.println("Marks: " + this.marks);
        System.out.println("Grade: " + this.grade);
    }
}

// Main application class
public class StudentApp {
    public static void main(String[] args) {
        // Use ArrayList to manage multiple student records
        List<Student> studentList = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);
        int choice = 0;

        // Loop for Menu Interaction
        do {
            // Display Menu
            System.out.println("\n===== Student Record Menu =====");
            System.out.println("1. Add Student");
            System.out.println("2. Display All Students");
            System.out.println("3. Exit");
            System.out.println("===============================");
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
                        // Add Student
                        Student newStudent = new Student();
                        newStudent.inputDetails(scanner);
                        studentList.add(newStudent);
                        System.out.println("Student added successfully!");
                        break;
                    case 2:
                        // Display All Students
                        if (studentList.isEmpty()) {
                            System.out.println("No records to display.");
                        } else {
                            System.out.println("\n--- Student Records ---");
                            // Loop through the list to display records
                            for (Student student : studentList) {
                                student.displayDetails();
                                System.out.println("---");
                            }
                            System.out.println("--- End of Records ---");
                        }
                        break;
                    case 3:
                        // Exit
                        System.out.println("Exiting the application. Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter 1, 2, or 3.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a numerical choice.");
                choice = 0;
            }
        } while (choice != 3);

        scanner.close();
    }
}