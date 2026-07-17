package com.attendance.system.repository;

import com.attendance.system.model.Grade;
import com.attendance.system.model.Subject;
import com.attendance.system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {

    List<Grade> findByStudent(User student);

    List<Grade> findBySubjectTeacher(User teacher);

    Optional<Grade> findByStudentAndSubjectAndExamType(User student, Subject subject, String examType);
}
