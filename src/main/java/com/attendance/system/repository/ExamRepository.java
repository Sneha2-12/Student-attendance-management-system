package com.attendance.system.repository;

import com.attendance.system.model.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findAllByOrderByExamDateAsc();
    List<Exam> findByExamDateBetween(LocalDate startDate, LocalDate endDate);
    List<Exam> findBySubjectTeacherEmail(String teacherEmail);
}
