package com.example.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JwtUtil {
    private static final String JWT_KEY = "JWT_SECRET_KEY";

    private JwtUtil() {
        // Utility class
    }

    private static final String SECRET = (System.getenv(JWT_KEY) != null && !System.getenv(JWT_KEY).isBlank())
            ? System.getenv(JWT_KEY)
            : "ReplaceThisWithASecureRandomSecretKeyOfSufficientLength123!";
    private static final long EXP_MS = 1000L * 60 * 60 * 24;

    private static Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public static String generateToken(String userId, String role, String name) {
        var builder = Jwts.builder()
                .setSubject(userId);

        if (name != null && !name.isBlank()) {
            builder.claim("name", name);
        }

        return builder
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
