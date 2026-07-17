package com.attendance.system.controller;

import com.attendance.system.model.*;
import com.attendance.system.service.AttendanceService;
import com.attendance.system.service.ExamService;
import com.attendance.system.service.LeaveService;
import com.attendance.system.service.UserService;
import com.attendance.system.repository.SubjectRepository;
import com.attendance.system.repository.TimetableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class ViewController {

    @Autowired
    private UserService userService;

    @Autowired
    private LeaveService leaveService;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private ExamService examService;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private TimetableRepository timetableRepository;

    @Autowired
    private com.attendance.system.repository.GradeRepository gradeRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return userService.findByEmail(auth.getName()).orElse(null);
    }

    @GetMapping("/login")
    public String login() {
        User user = getCurrentUser();
        if (user != null) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        User user = getCurrentUser();
        if (user != null) {
            return "redirect:/dashboard";
        }
        return "register";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        
        switch (user.getRole()) {
            case ROLE_ADMIN:
                return "redirect:/admin/dashboard";
            case ROLE_TEACHER:
                return "redirect:/teacher/dashboard";
            case ROLE_STUDENT:
                return "redirect:/student/dashboard";
            default:
                return "redirect:/login";
        }
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        // Metrics
        List<User> students = userService.getStudents();
        List<User> teachers = userService.getTeachers();
        List<LeaveRequest> pendingLeaves = leaveService.getPendingLeaves();
        List<Exam> exams = examService.getAllUpcomingExams();
        List<Subject> subjects = subjectRepository.findAll();

        model.addAttribute("user", user);
        model.addAttribute("totalStudents", students.size());
        model.addAttribute("totalTeachers", teachers.size());
        model.addAttribute("pendingLeavesCount", pendingLeaves.size());
        model.addAttribute("upcomingExamsCount", exams.size());
        
        model.addAttribute("students", userService.getAllStudentProfiles());
        model.addAttribute("teachers", teachers);
        model.addAttribute("pendingLeaves", pendingLeaves);
        model.addAttribute("subjects", subjects);
        model.addAttribute("exams", exams);

        return "admin-dashboard";
    }

    @GetMapping("/teacher/dashboard")
    public String teacherDashboard(Model model) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        Optional<TeacherProfile> profile = userService.getTeacherProfile(user);
        List<Subject> subjects = subjectRepository.findByTeacher(user);
        if (subjects.isEmpty()) {
            subjects = subjectRepository.findAll();
        }
        List<LeaveRequest> pendingLeaves = leaveService.getPendingLeaves();
        List<StudentProfile> students = userService.getAllStudentProfiles();

        model.addAttribute("user", user);
        model.addAttribute("profile", profile.orElse(null));
        model.addAttribute("subjects", subjects);
        model.addAttribute("pendingLeaves", pendingLeaves);
        model.addAttribute("students", students);

        return "teacher-dashboard";
    }

    @GetMapping("/student/dashboard")
    public String studentDashboard(Model model) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        Optional<StudentProfile> profile = userService.getStudentProfile(user);
        Map<String, Object> attendanceReport = attendanceService.getStudentAttendanceReport(user.getEmail());
        List<LeaveRequest> leaves = leaveService.getStudentLeaves(user);
        List<Subject> subjects = subjectRepository.findAll();

        model.addAttribute("user", user);
        model.addAttribute("profile", profile.orElse(null));
        model.addAttribute("attendance", attendanceReport);
        model.addAttribute("leaves", leaves);
        model.addAttribute("subjects", subjects);

        // Notifications & Alerts
        List<Exam> upcomingExams = examService.getAllUpcomingExams();
        model.addAttribute("upcomingExams", upcomingExams);

        if (profile.isPresent()) {
            String section = profile.get().getClassSection();
            List<TimetableEntry> timetable = timetableRepository.findByClassSection(section);
            List<TimetableEntry> cancelledClasses = timetable.stream().filter(TimetableEntry::isCancelled).toList();
            model.addAttribute("cancelledClasses", cancelledClasses);
        }

        boolean debarmentWarning = false;
        if (attendanceReport != null && attendanceReport.containsKey("academicPercentage")) {
            try {
                double percent = Double.parseDouble(attendanceReport.get("academicPercentage").toString());
                if (percent < 75.0) {
                    debarmentWarning = true;
                }
            } catch (Exception e) {}
        }
        model.addAttribute("debarmentWarning", debarmentWarning);

        return "student-dashboard";
    }

    @GetMapping("/calendar")
    public String calendar(Model model) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        model.addAttribute("role", user.getRole().name());
        model.addAttribute("subjects", subjectRepository.findAll());
        return "calendar";
    }

    @GetMapping("/timetable")
    public String timetable(Model model) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        model.addAttribute("role", user.getRole().name());

        if (user.getRole() == Role.ROLE_STUDENT) {
            Optional<StudentProfile> profile = userService.getStudentProfile(user);
            if (profile.isPresent()) {
                String section = profile.get().getClassSection();
                model.addAttribute("timetable", timetableRepository.findByClassSection(section));
                model.addAttribute("sectionName", section);
            } else {
                model.addAttribute("sectionName", "CS-A");
                model.addAttribute("timetable", timetableRepository.findByClassSection("CS-A"));
            }
        } else if (user.getRole() == Role.ROLE_TEACHER) {
            model.addAttribute("timetable", timetableRepository.findBySubjectTeacher(user));
        } else {
            // Admin: view all
            model.addAttribute("timetable", timetableRepository.findAll());
        }

        // Add drop down support data
        model.addAttribute("teachers", userService.getTeachers());
        model.addAttribute("subjects", subjectRepository.findAll());

        return "timetable";
    }

    @GetMapping("/students")
    public String listStudents(Model model) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";
        if (user.getRole() == Role.ROLE_STUDENT) {
            return "redirect:/dashboard"; // Students shouldn't view roster
        }

        model.addAttribute("user", user);
        model.addAttribute("role", user.getRole().name());
        model.addAttribute("students", userService.getAllStudentProfiles());

        return "students";
    }

    @GetMapping("/report-card")
    public String reportCard(Model model) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";
        if (user.getRole() != Role.ROLE_STUDENT) {
            return "redirect:/dashboard";
        }

        model.addAttribute("user", user);
        model.addAttribute("role", user.getRole().name());
        model.addAttribute("grades", gradeRepository.findByStudent(user));

        return "report-card";
    }

    @GetMapping("/gradebook")
    public String gradebook(Model model) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";
        if (user.getRole() == Role.ROLE_STUDENT) {
            return "redirect:/dashboard";
        }

        model.addAttribute("user", user);
        model.addAttribute("role", user.getRole().name());
        
        List<Subject> subjects;
        if (user.getRole() == Role.ROLE_TEACHER) {
            subjects = subjectRepository.findByTeacher(user);
        } else {
            subjects = subjectRepository.findAll();
        }

        model.addAttribute("subjects", subjects);
        model.addAttribute("students", userService.getAllStudentProfiles());
        model.addAttribute("teachers", userService.getTeachers());

        return "gradebook";
    }
}
