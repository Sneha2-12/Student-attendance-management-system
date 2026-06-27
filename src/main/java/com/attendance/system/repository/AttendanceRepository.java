package com.attendance.system.repository;

import com.attendance.system.model.Attendance;
import com.attendance.system.model.Subject;
import com.attendance.system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByStudent(User student);
    List<Attendance> findByStudentEmail(String email);
    List<Attendance> findByStudentEmailAndSubjectId(String email, Long subjectId);
    List<Attendance> findBySubjectAndDate(Subject subject, LocalDate date);
    List<Attendance> findBySubjectIdAndDate(Long subjectId, LocalDate date);
    Optional<Attendance> findByStudentAndSubjectAndDate(User student, Subject subject, LocalDate date);
    Optional<Attendance> findByStudentIdAndSubjectIdAndDate(Long studentId, Long subjectId, LocalDate date);
    List<Attendance> findByStudentAndDateBetween(User student, LocalDate startDate, LocalDate endDate);
}
