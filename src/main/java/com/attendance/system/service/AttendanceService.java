package com.attendance.system.service;

import com.attendance.system.model.*;
import com.attendance.system.repository.AttendanceRepository;
import com.attendance.system.repository.SubjectRepository;
import com.attendance.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public List<Attendance> markAttendance(Long subjectId, LocalDate date, Map<Long, AttendanceStatus> studentStatuses, User markedBy) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        List<Attendance> results = new ArrayList<>();

        for (Map.Entry<Long, AttendanceStatus> entry : studentStatuses.entrySet()) {
            Long studentId = entry.getKey();
            AttendanceStatus status = entry.getValue();

            User student = userRepository.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));

            Optional<Attendance> existing = attendanceRepository.findByStudentAndSubjectAndDate(student, subject, date);
            
            Attendance attendance;
            if (existing.isPresent()) {
                attendance = existing.get();
                // Override status unless it was already marked as a leave, in which case we still allow overriding, but warn
                attendance.setStatus(status);
                attendance.setMarkedBy(markedBy);
            } else {
                attendance = new Attendance(student, subject, date, status, markedBy);
            }
            results.add(attendanceRepository.save(attendance));
        }

        return results;
    }

    public List<Attendance> getAttendanceBySubjectAndDate(Long subjectId, LocalDate date) {
        return attendanceRepository.findBySubjectIdAndDate(subjectId, date);
    }

    public List<Attendance> getStudentAttendanceHistory(String email) {
        return attendanceRepository.findByStudentEmail(email);
    }

    public List<Attendance> getStudentAttendanceHistoryForSubject(String email, Long subjectId) {
        return attendanceRepository.findByStudentEmailAndSubjectId(email, subjectId);
    }

    public Map<String, Object> getStudentAttendanceReport(String email) {
        List<Attendance> records = attendanceRepository.findByStudentEmail(email);
        List<Subject> allSubjects = subjectRepository.findAll();

        int totalClasses = records.size();
        int totalPresent = 0;
        int totalAbsent = 0;
        int totalMedicalLeave = 0;
        int totalDutyLeave = 0;

        // Subject-wise stats map
        Map<Long, SubjectStats> subjectStatsMap = new HashMap<>();
        for (Subject sub : allSubjects) {
            subjectStatsMap.put(sub.getId(), new SubjectStats(sub.getName(), sub.getCourseCode()));
        }

        for (Attendance rec : records) {
            Long subId = rec.getSubject().getId();
            SubjectStats stats = subjectStatsMap.get(subId);
            if (stats == null) {
                stats = new SubjectStats(rec.getSubject().getName(), rec.getSubject().getCourseCode());
                subjectStatsMap.put(subId, stats);
            }

            stats.incrementTotal();

            switch (rec.getStatus()) {
                case PRESENT:
                    totalPresent++;
                    stats.incrementPresent();
                    break;
                case ABSENT:
                    totalAbsent++;
                    stats.incrementAbsent();
                    break;
                case MEDICAL_LEAVE:
                    totalMedicalLeave++;
                    stats.incrementMedicalLeave();
                    break;
                case DUTY_LEAVE:
                    totalDutyLeave++;
                    stats.incrementDutyLeave();
                    break;
            }
        }

        // Compute percentages
        double overallAcademicPercentage = totalClasses == 0 ? 100.0 : ((double) totalPresent / totalClasses) * 100.0;
        double overallEffectivePercentage = totalClasses == 0 ? 100.0 : 
                ((double) (totalPresent + totalMedicalLeave + totalDutyLeave) / totalClasses) * 100.0;

        // Format percentages for display
        for (SubjectStats stats : subjectStatsMap.values()) {
            stats.calculatePercentages();
        }

        Map<String, Object> report = new HashMap<>();
        report.put("totalClasses", totalClasses);
        report.put("present", totalPresent);
        report.put("absent", totalAbsent);
        report.put("medicalLeave", totalMedicalLeave);
        report.put("dutyLeave", totalDutyLeave);
        report.put("academicPercentage", Math.round(overallAcademicPercentage * 10.0) / 10.0);
        report.put("effectivePercentage", Math.round(overallEffectivePercentage * 10.0) / 10.0);
        report.put("subjectWiseStats", subjectStatsMap.values());

        return report;
    }

    // DTO class for subject stats
    public static class SubjectStats {
        private String subjectName;
        private String courseCode;
        private int total = 0;
        private int present = 0;
        private int absent = 0;
        private int medicalLeave = 0;
        private int dutyLeave = 0;
        private double academicPercentage = 100.0;
        private double effectivePercentage = 100.0;

        public SubjectStats(String subjectName, String courseCode) {
            this.subjectName = subjectName;
            this.courseCode = courseCode;
        }

        public void incrementTotal() { this.total++; }
        public void incrementPresent() { this.present++; }
        public void incrementAbsent() { this.absent++; }
        public void incrementMedicalLeave() { this.medicalLeave++; }
        public void incrementDutyLeave() { this.dutyLeave++; }

        public void calculatePercentages() {
            if (total > 0) {
                this.academicPercentage = Math.round(((double) present / total) * 100.0 * 10.0) / 10.0;
                this.effectivePercentage = Math.round(((double) (present + medicalLeave + dutyLeave) / total) * 100.0 * 10.0) / 10.0;
            }
        }

        public String getSubjectName() { return subjectName; }
        public String getCourseCode() { return courseCode; }
        public int getTotal() { return total; }
        public int getPresent() { return present; }
        public int getAbsent() { return absent; }
        public int getMedicalLeave() { return medicalLeave; }
        public int getDutyLeave() { return dutyLeave; }
        public double getAcademicPercentage() { return academicPercentage; }
        public double getEffectivePercentage() { return effectivePercentage; }
    }
}
