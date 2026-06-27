package com.attendance.system.repository;

import com.attendance.system.model.Subject;
import com.attendance.system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findByTeacher(User teacher);
    List<Subject> findByTeacherEmail(String email);
    Optional<Subject> findByCourseCode(String courseCode);
}
