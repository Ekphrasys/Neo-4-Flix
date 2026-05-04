package com.example.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {
    private static final String JWT_KEY = "JWT_SECRET_KEY"; // In production, use a secure key and store it safely

    private JwtUtil() {
        // Private constructor to prevent instantiation
    }

    // In a real deployment this should be stored in a secret manager
    // Read from environment variable JWT_SECRET for better flexibility in different environments.
    // Falls back to the original hard-coded value for quick local demos.
    private static final String SECRET = (System.getenv(JWT_KEY) != null && !System.getenv(JWT_KEY).isBlank())
            ? System.getenv(JWT_KEY)
            : "ReplaceThisWithASecureRandomSecretKeyOfSufficientLength123!";
    private static final long EXP_MS = 1000L * 60 * 60 * 24; // 24h

    private static Key getSigningKey() {
        byte[] keyBytes = SECRET.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public static String generateToken(String userId, String role, String name) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userId);
        claims.put("role", role != null ? role : "USER");
        if (name != null && !name.isBlank()) {
            claims.put("name", name);
        }

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXP_MS))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}