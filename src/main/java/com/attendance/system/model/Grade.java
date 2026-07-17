package com.attendance.system.model;

import jakarta.persistence.*;

@Entity
@Table(name = "grades", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "subject_id", "exam_type"})
})
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", referencedColumnName = "id", nullable = false)
    private User student; // Must have ROLE_STUDENT

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id", referencedColumnName = "id", nullable = false)
    private Subject subject;

    @Column(nullable = false)
    private Integer marksObtained;

    @Column(nullable = false)
    private Integer maxMarks = 100;

    @Column(nullable = false)
    private String examType; // e.g. "Midterm Exam", "Endterm Exam", "Assignment"

    @Column(length = 500)
    private String remarks;

    public Grade() {}

    public Grade(User student, Subject subject, Integer marksObtained, Integer maxMarks, String examType, String remarks) {
        this.student = student;
        this.subject = subject;
        this.marksObtained = marksObtained;
        this.maxMarks = maxMarks;
        this.examType = examType;
        this.remarks = remarks;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Integer getMarksObtained() {
        return marksObtained;
    }

    public void setMarksObtained(Integer marksObtained) {
        this.marksObtained = marksObtained;
    }

    public Integer getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(Integer maxMarks) {
        this.maxMarks = maxMarks;
    }

    public String getExamType() {
        return examType;
    }

    public void setExamType(String examType) {
        this.examType = examType;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    // Helper method to compute letter grade dynamically
    public String getLetterGrade() {
        if (marksObtained == null || maxMarks == null || maxMarks == 0) return "N/A";
        double percentage = (double) marksObtained / maxMarks * 100;
        if (percentage >= 90) return "A+";
        if (percentage >= 80) return "A";
        if (percentage >= 70) return "B";
        if (percentage >= 60) return "C";
        if (percentage >= 50) return "D";
        return "F";
    }
}
