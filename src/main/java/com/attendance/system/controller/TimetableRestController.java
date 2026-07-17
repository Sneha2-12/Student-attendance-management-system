package com.attendance.system.controller;

import com.attendance.system.model.Subject;
import com.attendance.system.model.TimetableEntry;
import com.attendance.system.model.User;
import com.attendance.system.repository.SubjectRepository;
import com.attendance.system.repository.TimetableRepository;
import com.attendance.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/timetable")
public class TimetableRestController {

    @Autowired
    private TimetableRepository timetableRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createEntry(@RequestBody Map<String, Object> payload) {
        try {
            Long subjectId = Long.valueOf(payload.get("subjectId").toString());
            Long teacherId = Long.valueOf(payload.get("teacherId").toString());
            String dayOfWeek = payload.get("dayOfWeek").toString();
            String timeSlot = payload.get("timeSlot").toString();
            String classSection = payload.get("classSection").toString();
            String roomNumber = payload.get("roomNumber").toString();

            Optional<Subject> subjectOpt = subjectRepository.findById(subjectId);
            Optional<User> teacherOpt = userRepository.findById(teacherId);

            if (subjectOpt.isEmpty() || teacherOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid subject or teacher ID"));
            }

            TimetableEntry entry = new TimetableEntry(
                    subjectOpt.get(),
                    teacherOpt.get(),
                    dayOfWeek,
                    timeSlot,
                    classSection,
                    roomNumber
            );

            timetableRepository.save(entry);
            return ResponseEntity.ok(Map.of("success", true, "message", "Timetable slot created successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Bad Request", "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateEntry(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Optional<TimetableEntry> entryOpt = timetableRepository.findById(id);
        if (entryOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            TimetableEntry entry = entryOpt.get();

            if (payload.containsKey("subjectId")) {
                Long subjectId = Long.valueOf(payload.get("subjectId").toString());
                subjectRepository.findById(subjectId).ifPresent(entry::setSubject);
            }

            if (payload.containsKey("teacherId")) {
                Long teacherId = Long.valueOf(payload.get("teacherId").toString());
                userRepository.findById(teacherId).ifPresent(entry::setTeacher);
            }

            if (payload.containsKey("dayOfWeek")) {
                entry.setDayOfWeek(payload.get("dayOfWeek").toString());
            }

            if (payload.containsKey("timeSlot")) {
                entry.setTimeSlot(payload.get("timeSlot").toString());
            }

            if (payload.containsKey("classSection")) {
                entry.setClassSection(payload.get("classSection").toString());
            }

            if (payload.containsKey("roomNumber")) {
                entry.setRoomNumber(payload.get("roomNumber").toString());
            }

            if (payload.containsKey("cancelled")) {
                entry.setCancelled(Boolean.parseBoolean(payload.get("cancelled").toString()));
            }

            timetableRepository.save(entry);
            return ResponseEntity.ok(Map.of("success", true, "message", "Timetable slot updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Bad Request", "message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleCancel(@PathVariable Long id) {
        Optional<TimetableEntry> entryOpt = timetableRepository.findById(id);
        if (entryOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        TimetableEntry entry = entryOpt.get();
        entry.setCancelled(!entry.isCancelled());
        timetableRepository.save(entry);

        String status = entry.isCancelled() ? "cancelled" : "activated";
        return ResponseEntity.ok(Map.of("success", true, "message", "Class session " + status + " successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteEntry(@PathVariable Long id) {
        if (!timetableRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        timetableRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Timetable slot deleted successfully"));
    }
}
