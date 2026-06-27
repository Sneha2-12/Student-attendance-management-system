package com.attendance.system.controller;

import com.attendance.system.model.Exam;
import com.attendance.system.model.Role;
import com.attendance.system.model.User;
import com.attendance.system.service.ExamService;
import com.attendance.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exams")
public class ExamRestController {

    @Autowired
    private ExamService examService;

    @Autowired
    private UserService userService;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return userService.findByEmail(auth.getName()).orElse(null);
    }

    @GetMapping
    public ResponseEntity<List<Exam>> getAllExams() {
        return ResponseEntity.ok(examService.getAllUpcomingExams());
    }

    @PostMapping("/schedule")
    public ResponseEntity<?> scheduleExam(@RequestBody ExamScheduleRequest request) {
        User user = getCurrentUser();
        if (user == null || user.getRole() == Role.ROLE_STUDENT) {
            return ResponseEntity.status(403).body("Unauthorized to schedule exams");
        }

        try {
            Exam exam = examService.scheduleExam(
                    request.getSubjectId(),
                    request.getExamDate(),
                    request.getStartTime(),
                    request.getEndTime(),
                    request.getRoom(),
                    request.getTotalMarks()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Exam scheduled successfully");
            response.put("examId", exam.getId());
            response.put("subject", exam.getSubject().getName());
            response.put("date", exam.getExamDate().toString());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExam(@PathVariable Long id) {
        User user = getCurrentUser();
        if (user == null || user.getRole() == Role.ROLE_STUDENT) {
            return ResponseEntity.status(403).body("Unauthorized to delete exams");
        }

        try {
            examService.deleteExam(id);
            return ResponseEntity.ok(Map.of("message", "Exam cancelled successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // DTO
    public static class ExamScheduleRequest {
        private Long subjectId;
        private LocalDate examDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private String room;
        private Integer totalMarks;

        public Long getSubjectId() { return subjectId; }
        public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }
        public LocalDate getExamDate() { return examDate; }
        public void setExamDate(LocalDate examDate) { this.examDate = examDate; }
        public LocalTime getStartTime() { return startTime; }
        public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
        public LocalTime getEndTime() { return endTime; }
        public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
        public String getRoom() { return room; }
        public void setRoom(String room) { this.room = room; }
        public Integer getTotalMarks() { return totalMarks; }
        public void setTotalMarks(Integer totalMarks) { this.totalMarks = totalMarks; }
    }
}
