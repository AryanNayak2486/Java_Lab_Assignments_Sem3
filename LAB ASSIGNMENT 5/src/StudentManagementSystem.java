import java.io.*;
import java.util.*;

// --- 1. Custom Exception (Lab 3) ---
class StudentNotFoundException extends Exception {
    public StudentNotFoundException(int rollNo) {
        super("Error: Student with Roll No. " + rollNo + " not found.");
    }
}

// --- 2. Multithreading Utility (Lab 3) ---
class Loader implements Runnable {
    private final String operation;

    public Loader(String operation) {
        this.operation = operation;
    }

    @Override
    public void run() {
        System.out.print(operation + ".....");
        try {
            for (int i = 0; i < 3; i++) {
                Thread.sleep(300);
                System.out.print(".");
            }
            System.out.println();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Operation interrupted.");
        }
    }
}

// --- 3. File Handling Utility (Lab 4) ---
class FileUtil {
    private static final String FILE_NAME = "students.txt";

    public static Map<Integer, Student> loadStudents() {
        Map<Integer, Student> studentMap = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    try {
                        Integer rollNo = Integer.valueOf(parts[0].trim());
                        String name = parts[1].trim();
                        String email = parts[2].trim();
                        String course = parts[3].trim();
                        Double marks = Double.valueOf(parts[4].trim());
                        Student s = new Student(rollNo, name, email, course, marks);
                        studentMap.put(rollNo, s);
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping malformed record in file: " + line);
                    }
                }
            }
            System.out.println("\nSuccessfully loaded " + studentMap.size() + " records from " + FILE_NAME);
        } catch (FileNotFoundException e) {
            System.out.println("Data file not found. Starting with an empty student list.");
        } catch (IOException e) {
            System.err.println("Error reading student records from file: " + e.getMessage());
        }
        return studentMap;
    }

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

// --- 4. Abstract Class (Lab 2) ---
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

// --- 5. Student Class (Lab 1, 2, 3, 4) ---
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

    public void calculateGrade() {
        if (marks == null || marks < 0 || marks > 100) {
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

    @Override
    public void displayInfo() {
        System.out.println("Roll No: " + this.rollNo);
        System.out.println("Name: " + getName());
        System.out.println("Email: " + getEmail());
        System.out.println("Course: " + this.course);
        System.out.println("Marks: " + (this.marks != null ? this.marks : "N/A"));
        System.out.println("Grade: " + this.grade);
    }

    // Getters
    public Integer getRollNo() { return rollNo; }
    public String getCourse() { return course; }
    public Double getMarks() { return marks; }
    public void setMarks(Double marks) {
        this.marks = marks;
        calculateGrade();
    }

    public static Comparator<Student> MarksComparator = (s1, s2) -> {
        if (s1.getMarks() == null && s2.getMarks() == null) return 0;
        if (s1.getMarks() == null) return -1;
        if (s2.getMarks() == null) return 1;
        return s2.getMarks().compareTo(s1.getMarks());
    };

    public static Comparator<Student> NameComparator = (s1, s2) -> {
        return s1.getName().compareToIgnoreCase(s2.getName());
    };
}

// --- 6. Interface (Lab 2) ---
interface RecordActions {
    void addStudent(Student s);
    void deleteStudent(int rollNo) throws StudentNotFoundException;
    void updateStudentMarks(int rollNo, Double newMarks) throws StudentNotFoundException;
    void searchStudent(int rollNo) throws StudentNotFoundException;
    void viewAllStudents(int sortOption);
    void saveAndExit(); // <-- Requires implementation in StudentManager
}

// --- 7. Service Class (Lab 2, 3, 4, 5) ---
class StudentManager implements RecordActions {

    private final Map<Integer, Student> studentMap;
    private final Scanner scanner;

    public StudentManager() {
        this.studentMap = FileUtil.loadStudents();
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void addStudent(Student s) {
        if (studentMap.containsKey(s.getRollNo())) {
            System.out.println("Error: Student with Roll No. " + s.getRollNo() + " already exists. Cannot add.");
            return;
        }
        Thread loaderThread = new Thread(new Loader("Adding student record"));
        loaderThread.start();
        try { loaderThread.join(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        studentMap.put(s.getRollNo(), s);
        System.out.println("\nStudent added successfully.");
        s.displayInfo();
    }

    @Override
    public void deleteStudent(int rollNo) throws StudentNotFoundException {
        if (studentMap.remove(rollNo) == null) {
            throw new StudentNotFoundException(rollNo);
        }
        Thread loaderThread = new Thread(new Loader("Deleting record"));
        loaderThread.start();
        try { loaderThread.join(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        System.out.println("Student with Roll No. " + rollNo + " deleted successfully.");
    }

    @Override
    public void updateStudentMarks(int rollNo, Double newMarks) throws StudentNotFoundException {
        Student s = studentMap.get(rollNo);
        if (s == null) {
            throw new StudentNotFoundException(rollNo);
        }
        Thread loaderThread = new Thread(new Loader("Updating marks"));
        loaderThread.start();
        try { loaderThread.join(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        s.setMarks(newMarks);
        System.out.println("\nStudent with Roll No. " + rollNo + " marks updated and grade recalculated.");
        s.displayInfo();
    }

    @Override
    public void searchStudent(int rollNo) throws StudentNotFoundException {
        Student s = studentMap.get(rollNo);
        if (s == null) {
            throw new StudentNotFoundException(rollNo);
        }
        System.out.println("--- Student Found ---");
        s.displayInfo();
        System.out.println("---------------------");
    }

    @Override
    public void viewAllStudents(int sortOption) {
        if (studentMap.isEmpty()) {
            System.out.println("The student list is currently empty.");
            return;
        }

        List<Student> students = new ArrayList<>(studentMap.values());

        switch (sortOption) {
            case 1:
                students.sort(Student.MarksComparator);
                System.out.println("\n--- Sorted Student List by MARKS (Descending) ---");
                break;
            case 2:
                students.sort(Student.NameComparator);
                System.out.println("\n--- Sorted Student List by NAME (Ascending) ---");
                break;
            case 0:
            default:
                System.out.println("\n--- All Student Records (Unsorted) ---");
                break;
        }

        Iterator<Student> iterator = students.iterator();
        while (iterator.hasNext()) {
            iterator.next().displayInfo();
            System.out.println("---------------------");
        }
    }

    @Override // <-- This annotation signals implementation of the interface method
    public void saveAndExit() {
        // Implementation of saveAndExit() (Lab 3 & 4 requirements)
        Thread loaderThread = new Thread(new Loader("Saving data to file"));
        loaderThread.start();
        try { loaderThread.join(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        FileUtil.saveStudents(studentMap);
        System.out.println("Exiting application. Goodbye!");
        scanner.close();
    }

    public void inputAndAddStudent() {
        System.out.println("--- Add New Student ---");

        try {
            System.out.print("Enter Roll No (Integer): ");
            Integer rollNo = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter Name: ");
            String name = scanner.nextLine().trim();

            System.out.print("Enter Email: ");
            String email = scanner.nextLine().trim();

            System.out.print("Enter Course: ");
            String course = scanner.nextLine().trim();

            System.out.print("Enter Marks (Double, 0-100): ");
            Double marks = Double.parseDouble(scanner.nextLine().trim());

            if (name.isEmpty() || course.isEmpty()) {
                System.out.println("\nError: Name or course cannot be empty.");
                return;
            }

            if (marks < 0 || marks > 100) {
                System.out.println("\nError: Invalid input. Marks must be between 0 and 100.");
                return;
            }

            Student newStudent = new Student(rollNo, name, email, course, marks);
            addStudent(newStudent);

        } catch (NumberFormatException e) {
            System.out.println("\nError: Invalid input format for Roll No or Marks. Please enter valid numbers.");
        } catch (Exception e) {
            System.out.println("\nAn unexpected error occurred: " + e.getMessage());
        }
    }
}

// --- 8. Main Entry Point ---
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
                int rollNo;

                switch (choice) {
                    case 1:
                        manager.inputAndAddStudent();
                        break;
                    case 2:
                        manager.viewAllStudents(0);
                        break;
                    case 3:
                        System.out.print("Enter Roll No to search: ");
                        rollNo = Integer.parseInt(mainScanner.nextLine().trim());
                        manager.searchStudent(rollNo);
                        break;
                    case 4:
                        System.out.print("Enter Roll No to update marks: ");
                        rollNo = Integer.parseInt(mainScanner.nextLine().trim());
                        System.out.print("Enter New Marks (Double, 0-100): ");
                        Double newMarks = Double.parseDouble(mainScanner.nextLine().trim());
                        if (newMarks < 0 || newMarks > 100) {
                            System.out.println("Error: Marks must be between 0 and 100.");
                        } else {
                            manager.updateStudentMarks(rollNo, newMarks);
                        }
                        break;
                    case 5:
                        System.out.print("Enter Roll No to delete: ");
                        rollNo = Integer.parseInt(mainScanner.nextLine().trim());
                        manager.deleteStudent(rollNo);
                        break;
                    case 6:
                        manager.viewAllStudents(1);
                        break;
                    case 7:
                        manager.viewAllStudents(2);
                        break;
                    case 8:
                        manager.saveAndExit();
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number for your choice or data.");
            } catch (StudentNotFoundException e) {
                System.out.println(e.getMessage());
            }

        } while (choice != 8);
    }

    private static void displayMenu() {
        System.out.println("\n===== Capstone Student Management System (L1-L5) =====");
        System.out.println("1. Add Student (OOP, Validation, Threading)");
        System.out.println("2. View All Students (Unsorted)");
        System.out.println("3. Search by Roll No (Exception Handling)");
        System.out.println("4. Update Marks by Roll No");
        System.out.println("5. Delete by Roll No");
        System.out.println("6. Sort and View by Marks (Descending)");
        System.out.println("7. Sort and View by Name (Ascending)");
        System.out.println("8. Save and Exit (File Persistence, Threading)");
        System.out.println("======================================================");
    }
}