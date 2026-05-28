package com.example.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    @Test
    void testGenerateAndParseToken() {
        String token = JwtUtil.generateToken("user123", "John Doe");
        assertNotNull(token);
        
        Claims claims = JwtUtil.parseToken(token);
        assertEquals("user123", claims.getSubject());
        assertEquals("John Doe", claims.get("name"));
    }

    @Test
    void testGenerateTokenWithoutName() {
        String token = JwtUtil.generateToken("user456", null);
        assertNotNull(token);
        
        Claims claims = JwtUtil.parseToken(token);
        assertEquals("user456", claims.getSubject());
        assertNull(claims.get("name"));
    }

    @Test
    void testGenerateAndParseTempToken() {
        String token = JwtUtil.generateTempToken("user789");
        assertNotNull(token);
        
        Claims claims = JwtUtil.parseToken(token);
        assertEquals("user789", claims.getSubject());
        assertEquals("2fa-pending", claims.get("purpose"));
    }

    @Test
    void testParseInvalidTokenThrowsException() {
        assertThrows(Exception.class, () -> {
            JwtUtil.parseToken("invalid.token.here");
        });
    }
}
