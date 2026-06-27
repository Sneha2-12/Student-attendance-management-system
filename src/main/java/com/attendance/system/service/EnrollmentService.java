package com.attendance.system.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendance.system.model.Enrollment;
import com.attendance.system.model.StudentProfile;
import com.attendance.system.model.Subject;
import com.attendance.system.model.User;
import com.attendance.system.repository.EnrollmentRepository;
import com.attendance.system.repository.SubjectRepository;
import com.attendance.system.repository.UserRepository;

@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Enrollment enrollStudentInSubject(Long studentId, Long subjectId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        // Check if already enrolled
        if (enrollmentRepository.existsByStudentAndSubject(student, subject)) {
            throw new IllegalArgumentException("Student is already enrolled in this subject");
        }

        Enrollment enrollment = new Enrollment(student, subject);
        return enrollmentRepository.save(enrollment);
    }

    @Transactional
    public void unenrollStudentFromSubject(Long studentId, Long subjectId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        Optional<Enrollment> enrollment = enrollmentRepository.findByStudentAndSubject(student, subject);
        if (enrollment.isPresent()) {
            enrollmentRepository.delete(enrollment.get());
        } else {
            throw new IllegalArgumentException("Student is not enrolled in this subject");
        }
    }

    public List<StudentProfile> getStudentsEnrolledInSubject(Long subjectId) {
        List<Enrollment> enrollments = enrollmentRepository.findBySubjectId(subjectId);
        return enrollments.stream()
                .map(e -> {
                    Optional<StudentProfile> profile = userRepository.findById(e.getStudent().getId())
                            .flatMap(user -> {
                                // Get StudentProfile from the enrollment
                                User student = e.getStudent();
                                return userRepository.findById(student.getId()).map(u -> {
                                    // This is a workaround - we'll need to add a method in StudentProfileRepository
                                    return null;
                                });
                            });
                    return profile.orElse(null);
                })
                .filter(p -> p != null)
                .collect(Collectors.toList());
    }

    public List<Enrollment> getEnrollmentsBySubject(Long subjectId) {
        return enrollmentRepository.findBySubjectId(subjectId);
    }

    public List<Subject> getSubjectsForStudent(Long studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        return enrollments.stream()
                .map(Enrollment::getSubject)
                .collect(Collectors.toList());
    }

    public boolean isStudentEnrolledInSubject(Long studentId, Long subjectId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        return enrollmentRepository.existsByStudentAndSubject(student, subject);
    }
}
