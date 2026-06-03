package com.example.controller;

import com.example.model.User;
import com.example.repository.UserRepository;
import com.example.security.JwtUtil;
import com.example.security.TotpService;
import com.example.validation.PasswordValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final TotpService totpService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final String ERROR_KEY = "error";

    public AuthController(UserRepository userRepository, TotpService totpService) {
        this.userRepository = userRepository;
        this.totpService = totpService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String email = body.get("email");
        String password = body.get("password");

        List<String> passwordErrors = PasswordValidator.validate(password);
        if (!passwordErrors.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, String.join("; ", passwordErrors)));
        }

        if (!userRepository.findByEmail(email).isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "Email already in use"));
        }
        String hashed = passwordEncoder.encode(password);
        User u = new User(username, email, hashed);

        // Enable 2FA by default for all new users
        String secret = totpService.generateSecret();
        u.setTwoFactorSecret(secret);
        u.setTwoFactorEnabled(true);

        userRepository.save(u);
        String token = JwtUtil.generateToken(u.getId().toString(), u.getUsername());
        String qrCodeUri = totpService.buildOtpAuthUri(secret, email);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("userId", u.getId());
        response.put("qrCodeUri", qrCodeUri);
        response.put("secret", secret);
        return ResponseEntity.ok(response);
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

        // If 2FA is enabled, return a temporary token instead of the real JWT
        if (u.isTwoFactorEnabled()) {
            String tempToken = JwtUtil.generateTempToken(u.getId());
            Map<String, String> response = new HashMap<>();
            response.put("requires2FA", "true");
            response.put("tempToken", tempToken);
            return ResponseEntity.ok(response);
        }

        String displayName = u.getUsername();
        if (displayName == null || displayName.isBlank()) {
            displayName = u.getEmail();
        }
        String token = JwtUtil.generateToken(u.getId(), displayName);
        return ResponseEntity.ok(Map.of("token", token, "userId", u.getId()));
    }

    /**
     * Step 2 of login: verify the TOTP code when 2FA is enabled.
     */
    @PostMapping("/verify-2fa")
    public ResponseEntity<Map<String, String>> verify2fa(@RequestBody Map<String, String> body) {
        String tempToken = body.get("tempToken");
        String codeStr = body.get("code");

        if (tempToken == null || codeStr == null) {
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "tempToken and code are required"));
        }

        // Validate the temporary token
        String userId;
        try {
            var claims = JwtUtil.parseToken(tempToken);
            if (!"2fa-pending".equals(claims.get("purpose"))) {
                return ResponseEntity.status(401).body(Map.of(ERROR_KEY, "Invalid temporary token"));
            }
            userId = claims.getSubject();
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(ERROR_KEY, "Invalid or expired temporary token"));
        }

        User u = userRepository.findById(userId).orElse(null);
        if (u == null) {
            return ResponseEntity.status(401).body(Map.of(ERROR_KEY, "User not found"));
        }

        int code;
        try {
            code = Integer.parseInt(codeStr);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "Invalid code format"));
        }

        if (!totpService.verifyCode(u.getTwoFactorSecret(), code)) {
            return ResponseEntity.status(401).body(Map.of(ERROR_KEY, "Invalid 2FA code"));
        }

        String displayName = u.getUsername();
        if (displayName == null || displayName.isBlank()) {
            displayName = u.getEmail();
        }
        String token = JwtUtil.generateToken(u.getId(), displayName);
        return ResponseEntity.ok(Map.of("token", token, "userId", u.getId()));
    }

    /**
     * Generate a TOTP secret and return the QR code URI.
     * Requires a valid JWT (authenticated user).
     */
    @PostMapping("/2fa/setup")
    public ResponseEntity<Map<String, String>> setup2fa(@RequestHeader("Authorization") String authHeader) {
        User u = getUserFromAuth(authHeader);
        if (u == null) {
            return ResponseEntity.status(401).body(Map.of(ERROR_KEY, "Unauthorized"));
        }

        String secret = totpService.generateSecret();
        u.setTwoFactorSecret(secret);
        userRepository.save(u);

        String qrCodeUri = totpService.buildOtpAuthUri(secret, u.getEmail());
        Map<String, String> response = new HashMap<>();
        response.put("secret", secret);
        response.put("qrCodeUri", qrCodeUri);
        return ResponseEntity.ok(response);
    }

    /**
     * Confirm the 2FA setup by validating the first TOTP code.
     * This permanently enables 2FA on the account.
     */
    @PostMapping("/2fa/confirm")
    public ResponseEntity<Map<String, String>> confirm2fa(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body) {
        User u = getUserFromAuth(authHeader);
        if (u == null) {
            return ResponseEntity.status(401).body(Map.of(ERROR_KEY, "Unauthorized"));
        }

        if (u.getTwoFactorSecret() == null) {
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "2FA setup not initiated. Call /2fa/setup first."));
        }

        String codeStr = body.get("code");
        if (codeStr == null) {
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "code is required"));
        }

        int code;
        try {
            code = Integer.parseInt(codeStr);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "Invalid code format"));
        }

        if (!totpService.verifyCode(u.getTwoFactorSecret(), code)) {
            return ResponseEntity.status(400).body(Map.of(ERROR_KEY, "Invalid code. Please try again."));
        }

        u.setTwoFactorEnabled(true);
        userRepository.save(u);
        return ResponseEntity.ok(Map.of("message", "2FA has been enabled successfully"));
    }

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> searchUsers(@RequestParam(value = "q", required = false) String query) {
        List<User> users;
        if (query == null || query.isBlank()) {
            users = userRepository.findAll();
        } else {
            users = userRepository.findByUsernameContainingIgnoreCase(query);
        }
        List<Map<String, Object>> result = users.stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("username", u.getUsername());
            map.put("email", u.getEmail());
            return map;
        }).toList();
        return ResponseEntity.ok(result);
    }

    /**
     * Extract the authenticated user from the Authorization header.
     */
    private User getUserFromAuth(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        try {
            String token = authHeader.substring(7);
            var claims = JwtUtil.parseToken(token);
            String userId = claims.getSubject();
            return userRepository.findById(userId).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}