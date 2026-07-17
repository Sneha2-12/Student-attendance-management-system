package com.attendance.system.config;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.attendance.system.model.Attendance;
import com.attendance.system.model.AttendanceStatus;
import com.attendance.system.model.Enrollment;
import com.attendance.system.model.Exam;
import com.attendance.system.model.Role;
import com.attendance.system.model.Subject;
import com.attendance.system.model.User;
import com.attendance.system.repository.AttendanceRepository;
import com.attendance.system.repository.EnrollmentRepository;
import com.attendance.system.repository.ExamRepository;
import com.attendance.system.repository.GradeRepository;
import com.attendance.system.repository.SubjectRepository;
import com.attendance.system.repository.TimetableRepository;
import com.attendance.system.repository.UserRepository;
import com.attendance.system.service.UserService;
import com.attendance.system.model.TimetableEntry;
import com.attendance.system.model.Grade;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private TimetableRepository timetableRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            return; // Database already seeded
        }

        System.out.println("Seeding database with demo school records...");

        // 1. Create Admins
        User admin = userService.registerUser(
                "admin@school.com", "admin123", "Principal Skinner", Role.ROLE_ADMIN,
                null, null, null, null
        );

        // 2. Create Teachers
        User teacher1 = userService.registerUser(
                "teacher1@school.com", "teacher123", "Dr. Sarah Connor", Role.ROLE_TEACHER,
                null, null, "EMP101", "Computer Science"
        );

        User teacher2 = userService.registerUser(
                "teacher2@school.com", "teacher123", "Prof. James Smith", Role.ROLE_TEACHER,
                null, null, "EMP102", "Information Technology"
        );

        // 3. Create Students
        User student1 = userService.registerUser(
                "student1@school.com", "student123", "John Doe", Role.ROLE_STUDENT,
                "CS202301", "CS-A", null, null
        );

        User student2 = userService.registerUser(
                "student2@school.com", "student123", "Jane Smith", Role.ROLE_STUDENT,
                "CS202302", "CS-A", null, null
        );

        User student3 = userService.registerUser(
                "student3@school.com", "student123", "Bob Johnson", Role.ROLE_STUDENT,
                "CS202303", "CS-B", null, null
        );

        // 4. Create Subjects
        Subject dsa = new Subject("Data Structures & Algorithms", "CS-101", teacher1);
        Subject web = new Subject("Web Development", "CS-102", teacher1);
        Subject dbms = new Subject("Database Management Systems", "CS-103", teacher2);

        dsa = subjectRepository.save(dsa);
        web = subjectRepository.save(web);
        dbms = subjectRepository.save(dbms);

        // 4.5 Enroll Students in Subjects
        // Student1 (John - CS-A) enrolled in DSA and Web
        enrollmentRepository.save(new Enrollment(student1, dsa));
        enrollmentRepository.save(new Enrollment(student1, web));
        
        // Student2 (Jane - CS-A) enrolled in DSA and Web
        enrollmentRepository.save(new Enrollment(student2, dsa));
        enrollmentRepository.save(new Enrollment(student2, web));
        
        // Student3 (Bob - CS-B) enrolled in DBMS and DSA
        enrollmentRepository.save(new Enrollment(student3, dbms));
        enrollmentRepository.save(new Enrollment(student3, dsa));

        // 5. Create Mock Exams (Upcoming in next 10-20 days)
        LocalDate today = LocalDate.now();
        
        Exam exam1 = new Exam(dsa, today.plusDays(5), LocalTime.of(10, 0), LocalTime.of(13, 0), "Block A - Room 101", 100);
        Exam exam2 = new Exam(dbms, today.plusDays(10), LocalTime.of(14, 0), LocalTime.of(17, 0), "Block B - Aud 2", 100);
        Exam exam3 = new Exam(web, today.plusDays(15), LocalTime.of(9, 30), LocalTime.of(11, 30), "Lab 4 (CS Block)", 50);

        examRepository.save(exam1);
        examRepository.save(exam2);
        examRepository.save(exam3);

        // 6. Create Historical Attendance (Past 14 days, excluding weekends)
        List<User> studentsList = List.of(student1, student2, student3);
        List<Subject> subjectsList = List.of(dsa, web, dbms);
        Random random = new Random();

        for (int i = 14; i >= 1; i--) {
            LocalDate date = today.minusDays(i);
            
            // Skip weekends
            if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                continue;
            }

            for (User student : studentsList) {
                for (Subject subject : subjectsList) {
                    // Decide status: 85% Present, 10% Absent, 5% Medical/Duty Leave
                    int roll = random.nextInt(100);
                    AttendanceStatus status;
                    if (roll < 85) {
                        status = AttendanceStatus.PRESENT;
                    } else if (roll < 95) {
                        status = AttendanceStatus.ABSENT;
                    } else if (roll < 98) {
                        status = AttendanceStatus.MEDICAL_LEAVE;
                    } else {
                        status = AttendanceStatus.DUTY_LEAVE;
                    }

                    // For Bob (student3), let's put it in CS-B section.
                    // Let's assume student1 and student2 attend CS-A, Bob attends CS-B.
                    // All can have attendance records for simplicity.
                    Attendance attendance = new Attendance(student, subject, date, status, teacher1);
                    attendanceRepository.save(attendance);
                }
            }
        }

        // 7. Create Timetable Entries
        // Section CS-A (DSA, Web Development, DBMS)
        timetableRepository.save(new TimetableEntry(dsa, dsa.getTeacher(), "MONDAY", "09:00 - 10:00", "CS-A", "Room 301"));
        timetableRepository.save(new TimetableEntry(web, web.getTeacher(), "MONDAY", "10:15 - 11:15", "CS-A", "Room 301"));
        
        timetableRepository.save(new TimetableEntry(web, web.getTeacher(), "TUESDAY", "09:00 - 10:00", "CS-A", "Room 301"));
        timetableRepository.save(new TimetableEntry(dbms, dbms.getTeacher(), "TUESDAY", "11:30 - 12:30", "CS-A", "Room 302"));
        
        timetableRepository.save(new TimetableEntry(dsa, dsa.getTeacher(), "WEDNESDAY", "09:00 - 10:00", "CS-A", "Room 301"));
        timetableRepository.save(new TimetableEntry(dbms, dbms.getTeacher(), "WEDNESDAY", "10:15 - 11:15", "CS-A", "Room 302"));
        
        timetableRepository.save(new TimetableEntry(web, web.getTeacher(), "THURSDAY", "09:00 - 10:00", "CS-A", "Room 301"));
        
        timetableRepository.save(new TimetableEntry(dsa, dsa.getTeacher(), "FRIDAY", "09:00 - 10:00", "CS-A", "Room 301"));
        timetableRepository.save(new TimetableEntry(dbms, dbms.getTeacher(), "FRIDAY", "11:30 - 12:30", "CS-A", "Room 302"));

        // Section CS-B (DBMS, DSA)
        timetableRepository.save(new TimetableEntry(dbms, dbms.getTeacher(), "MONDAY", "09:00 - 10:00", "CS-B", "Room 302"));
        timetableRepository.save(new TimetableEntry(dsa, dsa.getTeacher(), "MONDAY", "10:15 - 11:15", "CS-B", "Room 301"));
        
        timetableRepository.save(new TimetableEntry(dsa, dsa.getTeacher(), "TUESDAY", "09:00 - 10:00", "CS-B", "Room 301"));
        
        timetableRepository.save(new TimetableEntry(dbms, dbms.getTeacher(), "WEDNESDAY", "09:00 - 10:00", "CS-B", "Room 302"));
        timetableRepository.save(new TimetableEntry(dsa, dsa.getTeacher(), "WEDNESDAY", "11:30 - 12:30", "CS-B", "Room 301"));
        
        timetableRepository.save(new TimetableEntry(dbms, dbms.getTeacher(), "THURSDAY", "09:00 - 10:00", "CS-B", "Room 302"));
        
        timetableRepository.save(new TimetableEntry(dsa, dsa.getTeacher(), "FRIDAY", "10:15 - 11:15", "CS-B", "Room 301"));
        timetableRepository.save(new TimetableEntry(dbms, dbms.getTeacher(), "FRIDAY", "11:30 - 12:30", "CS-B", "Room 302"));

        // 8. Create Mock Grades
        gradeRepository.save(new Grade(student1, dsa, 85, 100, "Midterm Exam", "Excellent theoretical knowledge. Good job."));
        gradeRepository.save(new Grade(student1, web, 92, 100, "Midterm Exam", "Superb styling and visual design. Keep it up!"));
        
        gradeRepository.save(new Grade(student2, dsa, 74, 100, "Midterm Exam", "Satisfactory. Needs to spend more time on complex data structures."));
        gradeRepository.save(new Grade(student2, web, 88, 100, "Midterm Exam", "Very good presentation. Try to implement more responsive components."));

        gradeRepository.save(new Grade(student3, dsa, 68, 100, "Midterm Exam", "Needs improvement. Focus on algorithmic complexities."));
        gradeRepository.save(new Grade(student3, dbms, 95, 100, "Midterm Exam", "Outstanding query optimization skills. Exemplary work."));

        System.out.println("Database seeding completed successfully.");
    }
}
