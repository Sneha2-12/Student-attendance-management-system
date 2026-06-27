package com.attendance.system.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendance.system.model.Role;
import com.attendance.system.model.StudentProfile;
import com.attendance.system.model.TeacherProfile;
import com.attendance.system.model.User;
import com.attendance.system.repository.StudentProfileRepository;
import com.attendance.system.repository.TeacherProfileRepository;
import com.attendance.system.repository.UserRepository;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getStudents() {
        return userRepository.findByRole(Role.ROLE_STUDENT);
    }

    public List<User> getTeachers() {
        return userRepository.findByRole(Role.ROLE_TEACHER);
    }

    @Transactional
    public User registerUser(String email, String password, String name, Role role, 
                             String rollNumber, String classSection, 
                             String employeeId, String department) {
        
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email is already registered");
        }

        User user = new User(email, passwordEncoder.encode(password), name, role);
        user = userRepository.save(user);

        if (role == Role.ROLE_STUDENT) {
            if (rollNumber == null || rollNumber.trim().isEmpty() || classSection == null || classSection.trim().isEmpty()) {
                throw new IllegalArgumentException("Student roll number and class section are required");
            }
            StudentProfile profile = new StudentProfile(user, rollNumber, classSection);
            studentProfileRepository.save(profile);
        } else if (role == Role.ROLE_TEACHER) {
            if (employeeId == null || employeeId.trim().isEmpty()) {
                // Auto-generate employee ID if not provided
                employeeId = "EMP" + System.currentTimeMillis();
            }
            if (department == null || department.trim().isEmpty()) {
                throw new IllegalArgumentException("Department is required");
            }
            
            // Check if employee ID already exists
            if (teacherProfileRepository.existsByEmployeeId(employeeId)) {
                throw new IllegalArgumentException("Employee ID '" + employeeId + "' is already in use. Please use a unique Employee ID.");
            }
            
            TeacherProfile profile = new TeacherProfile(user, employeeId, department);
            teacherProfileRepository.save(profile);
        }

        return user;
    }

    public Optional<StudentProfile> getStudentProfile(User user) {
        return studentProfileRepository.findByUser(user);
    }

    public Optional<StudentProfile> getStudentProfileByEmail(String email) {
        return studentProfileRepository.findByUserEmail(email);
    }

    public Optional<TeacherProfile> getTeacherProfile(User user) {
        return teacherProfileRepository.findByUser(user);
    }

    public Optional<TeacherProfile> getTeacherProfileByEmail(String email) {
        return teacherProfileRepository.findByUserEmail(email);
    }

    public List<StudentProfile> getAllStudentProfiles() {
        return studentProfileRepository.findAll();
    }
}
