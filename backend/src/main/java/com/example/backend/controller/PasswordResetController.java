package com.example.backend.controller;

import com.example.backend.model.User;
import com.example.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reset")
public class PasswordResetController {

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/password")
    public ResponseEntity<String> resetPassword() {
        User user = userService.findByUsername("admin");
        if (user != null) {
            // 重置密码为'admin'
            user.setPasswordHash(passwordEncoder.encode("admin"));
            userService.update(user);
            return ResponseEntity.ok("Password reset successful");
        }
        return ResponseEntity.status(404).body("User not found");
    }
}