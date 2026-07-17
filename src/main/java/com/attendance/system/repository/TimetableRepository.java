package com.attendance.system.repository;

import com.attendance.system.model.TimetableEntry;
import com.attendance.system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimetableRepository extends JpaRepository<TimetableEntry, Long> {

    List<TimetableEntry> findByClassSection(String classSection);

    List<TimetableEntry> findBySubjectTeacher(User teacher);
}
