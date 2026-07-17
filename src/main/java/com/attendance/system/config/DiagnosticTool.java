package com.attendance.system.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DiagnosticTool {
    public static void main(String[] args) {
        String dbUrl = "jdbc:h2:file:./data/attendancedb";
        try (Connection conn = DriverManager.getConnection(dbUrl, "sa", "password")) {
            Statement stmt = conn.createStatement();
            
            System.out.println("=== USERS ===");
            ResultSet rsUsers = stmt.executeQuery("SELECT id, email, name, role FROM users");
            while (rsUsers.next()) {
                System.out.printf("User ID: %d | Email: %s | Name: %s | Role: %s\n",
                    rsUsers.getLong("id"), rsUsers.getString("email"), rsUsers.getString("name"), rsUsers.getString("role"));
            }
            
            System.out.println("\n=== STUDENT PROFILES ===");
            ResultSet rsStudent = stmt.executeQuery("SELECT id, user_id, roll_number, class_section FROM student_profiles");
            while (rsStudent.next()) {
                System.out.printf("ID: %d | User ID: %d | Roll: %s | Section: %s\n",
                    rsStudent.getLong("id"), rsStudent.getLong("user_id"), rsStudent.getString("roll_number"), rsStudent.getString("class_section"));
            }
            
            System.out.println("\n=== TIMETABLE ENTRIES ===");
            ResultSet rsTime = stmt.executeQuery("SELECT id, subject_id, teacher_id, day_of_week, time_slot, class_section, room_number, cancelled FROM timetable_entries");
            while (rsTime.next()) {
                System.out.printf("ID: %d | Sub ID: %d | Teach ID: %d | Day: %s | Slot: %s | Section: %s | Room: %s | Cancelled: %b\n",
                    rsTime.getLong("id"), rsTime.getLong("subject_id"), rsTime.getLong("teacher_id"),
                    rsTime.getString("day_of_week"), rsTime.getString("time_slot"), rsTime.getString("class_section"),
                    rsTime.getString("room_number"), rsTime.getBoolean("cancelled"));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
