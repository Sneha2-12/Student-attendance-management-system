package com.attendance.system.model;

import jakarta.persistence.*;

@Entity
@Table(name = "timetable_entries")
public class TimetableEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id", referencedColumnName = "id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id", referencedColumnName = "id", nullable = false)
    private User teacher; // Specific assigned teacher/substitute for this slot

    @Column(nullable = false)
    private String dayOfWeek; // MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY

    @Column(nullable = false)
    private String timeSlot; // e.g., "09:00 - 10:00", "10:15 - 11:15", "11:30 - 12:30", "13:30 - 14:30"

    @Column(nullable = false)
    private String classSection; // e.g., "CS-A", "CS-B"

    @Column(nullable = false)
    private String roomNumber; // e.g., "Room 301"

    @Column(nullable = false)
    private boolean cancelled = false; // Flag to indicate class cancellation

    public TimetableEntry() {}

    public TimetableEntry(Subject subject, User teacher, String dayOfWeek, String timeSlot, String classSection, String roomNumber) {
        this.subject = subject;
        this.teacher = teacher;
        this.dayOfWeek = dayOfWeek;
        this.timeSlot = timeSlot;
        this.classSection = classSection;
        this.roomNumber = roomNumber;
        this.cancelled = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public User getTeacher() {
        return teacher;
    }

    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public String getClassSection() {
        return classSection;
    }

    public void setClassSection(String classSection) {
        this.classSection = classSection;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
