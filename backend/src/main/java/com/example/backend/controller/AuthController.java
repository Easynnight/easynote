package com.example.backend.controller;

import com.example.backend.model.User;
import com.example.backend.model.dto.LoginRequest;
import com.example.backend.service.UserService;
import com.example.backend.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        logger.debug("Received login request for username: {}", loginRequest.getUsername());
        // 添加详细的登录请求信息日志
        logger.info("Login attempt from IP: {}", getClientIp());
        logger.info("Login request details - Username: {}", loginRequest.getUsername());
        try {
            // 进行身份验证
            logger.debug("Starting authentication for user: {}", loginRequest.getUsername());
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            // 设置认证信息到上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info("Login successful for user: {}", loginRequest.getUsername());

            // 生成JWT token
            String token = jwtUtil.generateToken(loginRequest.getUsername());
            logger.debug("Generated JWT token for user: {}", loginRequest.getUsername());

            // 返回token
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("username", loginRequest.getUsername());
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            logger.error("Login failed for username: {}. Error: {}", loginRequest.getUsername(), e.getMessage());
            logger.debug("Login failure details:", e);
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User registerRequest) {
        if (userService.findByUsername(registerRequest.getUsername()) != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username is already taken");
        }

        userService.save(registerRequest);
        return ResponseEntity.ok("User registered successfully");
    }

    // 添加获取客户端IP的辅助方法
    private String getClientIp() {
        // 在实际应用中，这里应该从请求中获取真实的客户端IP
        // 简化实现，返回一个占位符
        return "unknown-ip";
    }
}