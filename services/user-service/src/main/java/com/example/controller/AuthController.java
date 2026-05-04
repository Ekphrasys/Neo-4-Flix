package com.example.controller;

import com.example.model.User;
import com.example.repository.UserRepository;
import com.example.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final String ERROR_KEY = "error";

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String email = body.get("email");
        String password = body.get("password");

        if (!userRepository.findByEmail(email).isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "Email already in use"));
        }
        String hashed = passwordEncoder.encode(password);
        User u = new User(username, email, hashed);
        userRepository.save(u);
        String token = JwtUtil.generateToken(String.valueOf(u.getId()), "USER", u.getUsername());
        return ResponseEntity.ok(Map.of("token", token, "userId", String.valueOf(u.getId())));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        var opt = userRepository.findByEmail(email);
        if (opt.isEmpty()) return ResponseEntity.status(401).body(Map.of(ERROR_KEY, "Invalid credentials"));
        User u = opt.get(0);
        if (!passwordEncoder.matches(password, u.getPassword())) {
            return ResponseEntity.status(401).body(Map.of(ERROR_KEY, "Invalid credentials"));
        }
        String displayName = u.getUsername();
        if (displayName == null || displayName.isBlank()) {
            displayName = u.getEmail();
        }
        String token = JwtUtil.generateToken(String.valueOf(u.getId()), "USER", displayName);
        return ResponseEntity.ok(Map.of("token", token, "userId", String.valueOf(u.getId())));
    }
}