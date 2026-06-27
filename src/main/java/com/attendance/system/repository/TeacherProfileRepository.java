package com.attendance.system.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.attendance.system.model.TeacherProfile;
import com.attendance.system.model.User;

@Repository
public interface TeacherProfileRepository extends JpaRepository<TeacherProfile, Long> {
    Optional<TeacherProfile> findByUser(User user);
    Optional<TeacherProfile> findByUserEmail(String email);
    boolean existsByEmployeeId(String employeeId);
}
