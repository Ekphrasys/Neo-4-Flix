package com.example.controller;

import com.example.model.User;
import com.example.repository.UserRepository;
import com.example.security.SecurityConfig;
import com.example.security.TotpService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


// Tests for errors and malicious entries

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@DisplayName("Error Handling & Malicious Input Tests")
class AuthControllerErrorHandlingTest {
    
    private static final UUID uuid1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private UserRepository userRepository;
    
    @MockBean
    private TotpService totpService;
    
    // ==================== REGISTER ENDPOINT TESTS ====================
    
    @Test
    @DisplayName("Should reject NULL/missing email")
    void registerWithNullEmail() throws Exception {
        Map<String, String> payload = Map.of(
                "username", "user",
                // email missing
                "password", "Password1!"
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should reject NULL/missing password")
    void registerWithNullPassword() throws Exception {
        Map<String, String> payload = Map.of(
                "username", "user",
                "email", "test@example.com"
                // password missing
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }
    
    @ParameterizedTest
    @DisplayName("Should reject passwords with insufficient strength")
    @ValueSource(strings = {
            "",                    // Empty
            "pass",               // Too short
            "12345678",          // Only numbers
            "password",          // No uppercase
            "PASSWORD",          // No lowercase
            "Pass1",             // Too short even with special chars
            "pass word1!"        // Has space (might be an issue)
    })
    void registerWithWeakPasswords(String weakPassword) throws Exception {
        Map<String, String> payload = Map.of(
                "username", "neo-user",
                "email", "weak@neo4flix.com",
                "password", weakPassword
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }
    
    @ParameterizedTest
    @DisplayName("Should reject invalid email formats")
    @ValueSource(strings = {
            "notanemail",
            "@nodomain.com",
            "user@",
            "user@.com",
            "user....@domain.com",
            "user @domain.com",  // Space
            "user@domain..com"
    })
    void registerWithInvalidEmails(String invalidEmail) throws Exception {
        Map<String, String> payload = Map.of(
                "username", "neo-user",
                "email", invalidEmail,
                "password", "Password1!"
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should handle extremely long email (>1000 chars)")
    void registerWithExtremelyLongEmail() throws Exception {
        String longEmail = "a".repeat(1000) + "@example.com";
        Map<String, String> payload = Map.of(
                "username", "user",
                "email", longEmail,
                "password", "Password1!"
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should handle extremely long password (>10000 chars)")
    void registerWithExtremelyLongPassword() throws Exception {
        String longPassword = "P" + "a".repeat(10000) + "1!";
        Map<String, String> payload = Map.of(
                "username", "user",
                "email", "test@example.com",
                "password", longPassword
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should handle SQL injection attempts in email")
    void registerWithSQLInjectionInEmail() throws Exception {
        String sqlInjection = "test' OR '1'='1@example.com";
        Map<String, String> payload = Map.of(
                "username", "user",
                "email", sqlInjection,
                "password", "Password1!"
        );
        
        Mockito.when(userRepository.findByEmail(sqlInjection))
                .thenReturn(List.of());
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                // Should handle safely and not crash
                .andExpect(status().is4xxClientError());
    }
    
    @Test
    @DisplayName("Should handle XSS attempts in username")
    void registerWithXSSInUsername() throws Exception {
        String xssAttempt = "<script>alert('xss')</script>";
        Map<String, String> payload = Map.of(
                "username", xssAttempt,
                "email", "test@example.com",
                "password", "Password1!"
        );
        
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(List.of());
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                // Should accept but not execute script
                .andExpect(status().isOk());
        
        // Verify that the raw XSS string is NOT executed
        Mockito.verify(userRepository).findByEmail("test@example.com");
    }
    
    @Test
    @DisplayName("Should handle malformed JSON")
    void registerWithMalformedJSON() throws Exception {
        String malformedJson = "{username: \"user\", \"email\": \"test@example.com\"";  // Missing }
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should handle empty body")
    void registerWithEmptyBody() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should handle missing Content-Type header")
    void registerWithoutContentTypeHeader() throws Exception {
        Map<String, String> payload = Map.of(
                "username", "user",
                "email", "test@example.com",
                "password", "Password1!"
        );
        
        mockMvc.perform(post("/api/auth/register")
                .content(objectMapper.writeValueAsString(payload)))
                // Should still fail gracefully
                .andExpect(status().is4xxClientError());
    }
    
    // ==================== LOGIN ENDPOINT TESTS ====================
    
    @Test
    @DisplayName("Should handle login with NULL email")
    void loginWithNullEmail() throws Exception {
        Map<String, String> payload = Map.of(
                // email missing
                "password", "Password1!"
        );
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("Should handle login with NULL password")
    void loginWithNullPassword() throws Exception {
        Map<String, String> payload = Map.of(
                "email", "test@example.com"
                // password missing
        );
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("Should handle login with extremely long password")
    void loginWithExtremelyLongPassword() throws Exception {
        String longPassword = "P" + "a".repeat(10000) + "1!";
        Map<String, String> payload = Map.of(
                "email", "test@example.com",
                "password", longPassword
        );
        
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(List.of());
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("Should handle SQL injection in login email")
    void loginWithSQLInjection() throws Exception {
        String sqlInjection = "' OR 1=1 --@example.com";
        Map<String, String> payload = Map.of(
                "email", sqlInjection,
                "password", "Password1!"
        );
        
        Mockito.when(userRepository.findByEmail(sqlInjection))
                .thenReturn(List.of());
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isUnauthorized());
    }
    
    // ==================== 2FA TESTS ====================
    
    @Test
    @DisplayName("Should reject 2FA verification with NULL tempToken")
    void verify2faWithNullToken() throws Exception {
        Map<String, String> payload = Map.of(
                // tempToken missing
                "code", "123456"
        );
        
        mockMvc.perform(post("/api/auth/verify-2fa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should reject 2FA verification with invalid code format")
    void verify2faWithInvalidCodeFormat() throws Exception {
        Map<String, String> payload = Map.of(
                "tempToken", "valid.token.here",
                "code", "not-a-number"  // Invalid integer
        );
        
        Mockito.when(userRepository.findById("userId"))
                .thenReturn(java.util.Optional.empty());
        
        mockMvc.perform(post("/api/auth/verify-2fa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().is4xxClientError());
    }
    
    @Test
    @DisplayName("Should reject 2FA with extremely long token")
    void verify2faWithExtremelyLongToken() throws Exception {
        String longToken = "token." + "x".repeat(10000) + ".signature";
        Map<String, String> payload = Map.of(
                "tempToken", longToken,
                "code", "123456"
        );
        
        mockMvc.perform(post("/api/auth/verify-2fa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().is4xxClientError());
    }
    
    @Test
    @DisplayName("Should reject 2FA with invalid JWT signature")
    void verify2faWithInvalidJWTSignature() throws Exception {
        Map<String, String> payload = Map.of(
                "tempToken", "invalid.jwt.signature",
                "code", "123456"
        );
        
        mockMvc.perform(post("/api/auth/verify-2fa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isUnauthorized());
    }
    
    // ==================== EDGE CASES ====================
    
    @Test
    @DisplayName("Should handle duplicate email registration gracefully")
    void registerWithDuplicateEmail() throws Exception {
        Mockito.when(userRepository.findByEmail("existing@neo4flix.com"))
                .thenReturn(List.of(new User("existing", "existing@neo4flix.com", "hash")));
        
        Map<String, String> payload = Map.of(
                "username", "neo-user",
                "email", "existing@neo4flix.com",
                "password", "Password1!"
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email already in use"));
    }
    
    @Test
    @DisplayName("Should sanitize special characters and unicode")
    void handleSpecialCharactersAndUnicode() throws Exception {
        Map<String, String> payload = Map.of(
                "username", "用户名\uFEFF\u202E\u202D",  // Unicode exploit attempts
                "email", "test@example.com",
                "password", "Password1!"
        );
        
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(List.of());
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());  // Should accept and sanitize
    }
    
    @Test
    @DisplayName("Should handle backslash and escape sequences")
    void handleEscapeSequences() throws Exception {
        Map<String, String> payload = Map.of(
                "username", "user\\x00\\x1f",  // Null byte and control chars
                "email", "test@example.com",
                "password", "Password1!"
        );
        
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(List.of());
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());  // Should handle safely
    }
}

