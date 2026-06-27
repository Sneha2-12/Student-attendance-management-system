package com.attendance.system.repository;

import com.attendance.system.model.LeaveRequest;
import com.attendance.system.model.LeaveStatus;
import com.attendance.system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByStudent(User student);
    List<LeaveRequest> findByStudentEmail(String email);
    List<LeaveRequest> findByStatus(LeaveStatus status);
}
