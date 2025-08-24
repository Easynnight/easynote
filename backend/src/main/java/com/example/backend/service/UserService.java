package com.example.backend.service;

import com.example.backend.model.User;
import com.example.backend.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private UserMapper userMapper;

    public User save(User user) {
        // 处理密码字段 - 同时支持password和passwordHash字段
        try {
            // 尝试获取password字段（反射方式）
            java.lang.reflect.Field passwordField = User.class.getDeclaredField("password");
            passwordField.setAccessible(true);
            Object passwordValue = passwordField.get(user);
            if (passwordValue != null) {
                user.setPasswordHash(passwordEncoder.encode(passwordValue.toString()));
            }
        } catch (Exception e) {
            // 如果没有password字段，则使用passwordHash字段
            if (user.getPasswordHash() != null && !user.getPasswordHash().startsWith("$2a$")) {
                user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
            }
        }
        
        userMapper.insert(user);
        return user;
    }

    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    public User findByEmail(String email) {
        return userMapper.findByEmail(email);
    }

    public User findById(Long id) {
        return userMapper.findById(id);
    }

    public boolean update(User user) {
        // 处理密码字段 - 同时支持password和passwordHash字段
        try {
            // 尝试获取password字段（反射方式）
            java.lang.reflect.Field passwordField = User.class.getDeclaredField("password");
            passwordField.setAccessible(true);
            Object passwordValue = passwordField.get(user);
            if (passwordValue != null) {
                user.setPasswordHash(passwordEncoder.encode(passwordValue.toString()));
            }
        } catch (Exception e) {
            // 如果没有password字段，则使用passwordHash字段
            if (user.getPasswordHash() != null && !user.getPasswordHash().startsWith("$2a$")) {
                user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
            }
        }
        
        return userMapper.update(user) > 0;
    }

    public boolean delete(Long id) {
        return userMapper.delete(id) > 0;
    }

    // 验证用户密码
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}