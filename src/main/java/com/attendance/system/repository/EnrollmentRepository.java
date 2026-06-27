package com.attendance.system.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.attendance.system.model.Enrollment;
import com.attendance.system.model.Subject;
import com.attendance.system.model.User;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findBySubject(Subject subject);
    List<Enrollment> findByStudent(User student);
    Optional<Enrollment> findByStudentAndSubject(User student, Subject subject);
    List<Enrollment> findBySubjectId(Long subjectId);
    List<Enrollment> findByStudentId(Long studentId);
    boolean existsByStudentAndSubject(User student, Subject subject);
}
