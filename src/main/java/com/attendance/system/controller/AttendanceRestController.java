package com.attendance.system.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.attendance.system.model.Attendance;
import com.attendance.system.model.AttendanceStatus;
import com.attendance.system.model.User;
import com.attendance.system.service.AttendanceService;
import com.attendance.system.service.EnrollmentService;
import com.attendance.system.service.LeaveService;
import com.attendance.system.service.UserService;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceRestController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private UserService userService;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private LeaveService leaveService;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return userService.findByEmail(auth.getName()).orElse(null);
    }

    @PostMapping("/mark")
    public ResponseEntity<?> markAttendance(@RequestBody AttendanceSubmission submission) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        try {
            List<Attendance> marked = attendanceService.markAttendance(
                    submission.getSubjectId(),
                    submission.getDate(),
                    submission.getStudentStatuses(),
                    user
            );
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Attendance marked successfully");
            response.put("recordsCount", marked.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistory(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        if (subjectId != null && date != null) {
            List<Attendance> records = attendanceService.getAttendanceBySubjectAndDate(subjectId, date);
            return ResponseEntity.ok(records);
        }
        
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");

        if (user.getRole() == com.attendance.system.model.Role.ROLE_STUDENT) {
            return ResponseEntity.ok(attendanceService.getStudentAttendanceReport(user.getEmail()));
        }

        return ResponseEntity.badRequest().body("Please specify subjectId and date");
    }

    @GetMapping("/subject/{subjectId}/students")
    public ResponseEntity<?> getStudentsForSubject(@PathVariable Long subjectId) {
        try {
            List<com.attendance.system.model.Enrollment> enrollments = enrollmentService.getEnrollmentsBySubject(subjectId);
            
            List<StudentSubjectInfo> studentInfoList = enrollments.stream()
                    .map(enrollment -> {
                        User student = enrollment.getStudent();
                        com.attendance.system.model.StudentProfile profile = userService.getStudentProfileByEmail(student.getEmail()).orElse(null);
                        
                        // Check if student has any active leave requests
                        java.util.List<com.attendance.system.model.LeaveRequest> studentLeaves = leaveService.getStudentLeaves(student);
                        java.time.LocalDate today = java.time.LocalDate.now();
                        java.util.List<com.attendance.system.model.LeaveRequest> activeLeaves = studentLeaves.stream()
                                .filter(leave -> !leave.getStartDate().isAfter(today) && !leave.getEndDate().isBefore(today)
                                        && leave.getStatus() == com.attendance.system.model.LeaveStatus.APPROVED)
                                .toList();
                        
                        return new StudentSubjectInfo(
                                student.getId(),
                                student.getName(),
                                profile != null ? profile.getRollNumber() : "",
                                profile != null ? profile.getClassSection() : "",
                                activeLeaves.isEmpty() ? null : activeLeaves.get(0).getLeaveType().toString()
                        );
                    })
                    .sorted((a, b) -> {
                        String r1 = a.getRollNumber() != null ? a.getRollNumber() : "";
                        String r2 = b.getRollNumber() != null ? b.getRollNumber() : "";
                        return r1.compareTo(r2);
                    })
                    .toList();
            
            return ResponseEntity.ok(studentInfoList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    @PostMapping("/subject/{subjectId}/enroll/{studentId}")
    public ResponseEntity<?> enrollStudent(@PathVariable Long subjectId, @PathVariable Long studentId) {
        try {
            enrollmentService.enrollStudentInSubject(studentId, subjectId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Student enrolled successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }


    // DTO class for student-subject info
    public static class StudentSubjectInfo {
        private Long studentId;
        private String studentName;
        private String rollNumber;
        private String classSection;
        private String activeLeaveType; // "MEDICAL_LEAVE", "DUTY_LEAVE", or null

        public StudentSubjectInfo(Long studentId, String studentName, String rollNumber, String classSection, String activeLeaveType) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.rollNumber = rollNumber;
            this.classSection = classSection;
            this.activeLeaveType = activeLeaveType;
        }

        public Long getStudentId() { return studentId; }
        public String getStudentName() { return studentName; }
        public String getRollNumber() { return rollNumber; }
        public String getClassSection() { return classSection; }
        public String getActiveLeaveType() { return activeLeaveType; }
    }

    // DTO class
    public static class AttendanceSubmission {
        private Long subjectId;
        private LocalDate date;
        private Map<Long, AttendanceStatus> studentStatuses;

        public Long getSubjectId() { return subjectId; }
        public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        public Map<Long, AttendanceStatus> getStudentStatuses() { return studentStatuses; }
        public void setStudentStatuses(Map<Long, AttendanceStatus> studentStatuses) { this.studentStatuses = studentStatuses; }
    }
}
