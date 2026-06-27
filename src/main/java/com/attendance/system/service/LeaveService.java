package com.attendance.system.service;

import com.attendance.system.model.*;
import com.attendance.system.repository.AttendanceRepository;
import com.attendance.system.repository.LeaveRequestRepository;
import com.attendance.system.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class LeaveService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    public LeaveRequest applyForLeave(User student, LeaveType leaveType, LocalDate startDate, 
                                      LocalDate endDate, String reason, String documentPath) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        LeaveRequest request = new LeaveRequest(student, leaveType, startDate, endDate, reason, documentPath);
        return leaveRequestRepository.save(request);
    }

    public List<LeaveRequest> getStudentLeaves(User student) {
        return leaveRequestRepository.findByStudent(student);
    }

    public List<LeaveRequest> getStudentLeavesByEmail(String email) {
        return leaveRequestRepository.findByStudentEmail(email);
    }

    public List<LeaveRequest> getAllLeaves() {
        return leaveRequestRepository.findAll();
    }

    public List<LeaveRequest> getPendingLeaves() {
        return leaveRequestRepository.findByStatus(LeaveStatus.PENDING);
    }

    public Optional<LeaveRequest> getLeaveById(Long id) {
        return leaveRequestRepository.findById(id);
    }

    @Transactional
    public LeaveRequest processLeave(Long leaveId, LeaveStatus newStatus, User processedBy) {
        LeaveRequest request = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new RuntimeException("This leave request has already been processed");
        }

        request.setStatus(newStatus);
        request.setApprovedBy(processedBy);
        request = leaveRequestRepository.save(request);

        // If approved, dynamically update/override attendance for the student
        if (newStatus == LeaveStatus.APPROVED) {
            overrideAttendanceForLeave(request, processedBy);
        }

        return request;
    }

    private void overrideAttendanceForLeave(LeaveRequest request, User processedBy) {
        User student = request.getStudent();
        LeaveType leaveType = request.getLeaveType();
        AttendanceStatus attendanceStatus = (leaveType == LeaveType.MEDICAL) 
                ? AttendanceStatus.MEDICAL_LEAVE 
                : AttendanceStatus.DUTY_LEAVE;

        List<Subject> allSubjects = subjectRepository.findAll();

        // Loop through each date in the range (inclusive)
        LocalDate currentDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        while (!currentDate.isAfter(endDate)) {
            for (Subject subject : allSubjects) {
                Optional<Attendance> existingAttendance = attendanceRepository
                        .findByStudentAndSubjectAndDate(student, subject, currentDate);

                if (existingAttendance.isPresent()) {
                    Attendance attendance = existingAttendance.get();
                    // Override existing status to leave
                    attendance.setStatus(attendanceStatus);
                    attendance.setMarkedBy(processedBy);
                    attendanceRepository.save(attendance);
                } else {
                    // Create a new attendance record automatically marked as leave
                    Attendance attendance = new Attendance(student, subject, currentDate, attendanceStatus, processedBy);
                    attendanceRepository.save(attendance);
                }
            }
            currentDate = currentDate.plusDays(1);
        }
    }
}
