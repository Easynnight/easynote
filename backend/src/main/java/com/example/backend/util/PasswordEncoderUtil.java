package com.example.backend.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderUtil {
    // 加密密码的方法
    public static String encodePassword(String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(password);
    }

    public static void main(String[] args) {
//        if (args.length < 1) {
//            System.out.println("Usage: java PasswordEncoderUtil <password>");
//            return;
//        }
//        String password = args[0];
        String encodedPassword = encodePassword("ys007");
//        System.out.println("Original password: " + password);
        System.out.println("Encoded password: " + encodedPassword);
    }
}