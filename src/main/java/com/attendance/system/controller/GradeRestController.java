package com.attendance.system.controller;

import com.attendance.system.model.Grade;
import com.attendance.system.model.Subject;
import com.attendance.system.model.User;
import com.attendance.system.repository.GradeRepository;
import com.attendance.system.repository.SubjectRepository;
import com.attendance.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/grades")
public class GradeRestController {

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<?> saveGrade(@RequestBody Map<String, Object> payload) {
        try {
            Long studentId = Long.valueOf(payload.get("studentId").toString());
            Long subjectId = Long.valueOf(payload.get("subjectId").toString());
            Integer marksObtained = Integer.valueOf(payload.get("marksObtained").toString());
            Integer maxMarks = payload.containsKey("maxMarks") ? Integer.valueOf(payload.get("maxMarks").toString()) : 100;
            String examType = payload.get("examType").toString();
            String remarks = payload.containsKey("remarks") ? payload.get("remarks").toString() : "";

            Optional<User> studentOpt = userRepository.findById(studentId);
            Optional<Subject> subjectOpt = subjectRepository.findById(subjectId);

            if (studentOpt.isEmpty() || subjectOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid student or subject ID"));
            }

            User student = studentOpt.get();
            Subject subject = subjectOpt.get();

            // Look up existing grade to update or create new
            Optional<Grade> existingGradeOpt = gradeRepository.findByStudentAndSubjectAndExamType(student, subject, examType);
            Grade grade;
            if (existingGradeOpt.isPresent()) {
                grade = existingGradeOpt.get();
                grade.setMarksObtained(marksObtained);
                grade.setMaxMarks(maxMarks);
                grade.setRemarks(remarks);
            } else {
                grade = new Grade(student, subject, marksObtained, maxMarks, examType, remarks);
            }

            gradeRepository.save(grade);
            return ResponseEntity.ok(Map.of("success", true, "message", "Grade saved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Bad Request", "message", e.getMessage()));
        }
    }
}
