package com.attendance.system.service;

import com.attendance.system.model.Exam;
import com.attendance.system.model.Subject;
import com.attendance.system.repository.ExamRepository;
import com.attendance.system.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class ExamService {

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    public List<Exam> getAllUpcomingExams() {
        return examRepository.findAllByOrderByExamDateAsc();
    }

    public List<Exam> getExamsForTeacher(String teacherEmail) {
        return examRepository.findBySubjectTeacherEmail(teacherEmail);
    }

    public Exam scheduleExam(Long subjectId, LocalDate date, LocalTime startTime, LocalTime endTime, String room, Integer totalMarks) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Exam date cannot be in the past");
        }
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Start time cannot be after end time");
        }

        Exam exam = new Exam(subject, date, startTime, endTime, room, totalMarks);
        return examRepository.save(exam);
    }

    public void deleteExam(Long id) {
        examRepository.deleteById(id);
    }
}
