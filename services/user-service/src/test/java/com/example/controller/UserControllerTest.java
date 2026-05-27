package com.example.controller;

import com.example.model.User;
import com.example.repository.UserRepository;
import com.example.security.SecurityConfig;
import com.example.security.TotpService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
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

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class UserControllerTest {
    private static final UUID uuid1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID uuid2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID uuid3 = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private TotpService totpService;

    @Test
    void registerShouldReturnTokenAndQrCodeWhenEmailIsAvailable() throws Exception {
        Mockito.when(userRepository.findByEmail("new@neo4flix.com")).thenReturn(List.of());
        Mockito.when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(uuid1.toString());
            return saved;
        });
        Mockito.when(totpService.generateSecret()).thenReturn("JBSWY3DPEHPK3PXP");
        Mockito.when(totpService.buildOtpAuthUri("JBSWY3DPEHPK3PXP", "new@neo4flix.com"))
                .thenReturn("otpauth://totp/Neo-4-Flix:new%40neo4flix.com?secret=JBSWY3DPEHPK3PXP&issuer=Neo-4-Flix");

        Map<String, String> payload = Map.of(
                "username", "neo-user",
                "email", "new@neo4flix.com",
                "password", "Password1!"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(blankOrNullString())))
                .andExpect(jsonPath("$.userId", is(uuid1.toString())))
                .andExpect(jsonPath("$.qrCodeUri", not(blankOrNullString())))
                .andExpect(jsonPath("$.secret").value("JBSWY3DPEHPK3PXP"));
    }

    @Test
    void registerShouldRejectWeakPassword() throws Exception {
        Map<String, String> payload = Map.of(
                "username", "neo-user",
                "email", "weak@neo4flix.com",
                "password", "weak"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", not(blankOrNullString())));
    }

    @Test
    void registerShouldReturnBadRequestWhenEmailAlreadyExists() throws Exception {
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
    void loginShouldReturnUnauthorizedWhenUserDoesNotExist() throws Exception {
        Mockito.when(userRepository.findByEmail("missing@neo4flix.com")).thenReturn(List.of());

        Map<String, String> payload = Map.of(
                "email", "missing@neo4flix.com",
                "password", "password123"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    void loginShouldReturnUnauthorizedWhenPasswordIsInvalid() throws Exception {
        String encodedPassword = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("correct-password");
        User existingUser = new User("neo-user", "user@neo4flix.com", encodedPassword);
        existingUser.setId(uuid2.toString());

        Mockito.when(userRepository.findByEmail("user@neo4flix.com")).thenReturn(List.of(existingUser));

        Map<String, String> payload = Map.of(
                "email", "user@neo4flix.com",
                "password", "wrong-password"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    void loginShouldReturnTokenWhenCredentialsAreValid() throws Exception {
        String rawPassword = "password123";
        String encodedPassword = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(rawPassword);
        User existingUser = new User("neo-user", "user@neo4flix.com", encodedPassword);
        existingUser.setId(uuid3.toString());

        Mockito.when(userRepository.findByEmail("user@neo4flix.com")).thenReturn(List.of(existingUser));

        Map<String, String> payload = Map.of(
                "email", "user@neo4flix.com",
                "password", rawPassword
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(blankOrNullString())))
                .andExpect(jsonPath("$.userId").value(uuid3.toString()));
    }
}
