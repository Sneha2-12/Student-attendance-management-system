package com.attendance.system.controller;

import com.attendance.system.model.*;
import com.attendance.system.service.LeaveService;
import com.attendance.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/leaves")
public class LeaveRestController {

    @Autowired
    private LeaveService leaveService;

    @Autowired
    private UserService userService;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return userService.findByEmail(auth.getName()).orElse(null);
    }

    @PostMapping("/apply")
    public ResponseEntity<?> applyForLeave(@RequestBody LeaveSubmission submission) {
        User student = getCurrentUser();
        if (student == null || student.getRole() != Role.ROLE_STUDENT) {
            return ResponseEntity.status(403).body("Only students can apply for leaves");
        }

        try {
            LeaveRequest request = leaveService.applyForLeave(
                    student,
                    LeaveType.valueOf(submission.getLeaveType()),
                    submission.getStartDate(),
                    submission.getEndDate(),
                    submission.getReason(),
                    submission.getDocumentPath()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Leave application submitted successfully");
            response.put("leaveId", request.getId());
            response.put("status", request.getStatus().name());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/process/{id}")
    public ResponseEntity<?> processLeave(@PathVariable Long id, @RequestBody LeaveProcessRequest processRequest) {
        User user = getCurrentUser();
        if (user == null || user.getRole() == Role.ROLE_STUDENT) {
            return ResponseEntity.status(403).body("Unauthorized to process leaves");
        }

        try {
            LeaveStatus newStatus = LeaveStatus.valueOf(processRequest.getStatus());
            LeaveRequest processed = leaveService.processLeave(id, newStatus, user);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Leave processed successfully");
            response.put("leaveId", processed.getId());
            response.put("status", processed.getStatus().name());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // DTOs
    public static class LeaveSubmission {
        private String leaveType;
        private LocalDate startDate;
        private LocalDate endDate;
        private String reason;
        private String documentPath;

        public String getLeaveType() { return leaveType; }
        public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public String getDocumentPath() { return documentPath; }
        public void setDocumentPath(String documentPath) { this.documentPath = documentPath; }
    }

    public static class LeaveProcessRequest {
        private String status;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
