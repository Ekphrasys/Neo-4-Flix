package com.example.controller;

import com.example.dto.SharedRecommendationResponse;
import com.example.model.Genre;
import com.example.model.Movie;
import com.example.service.RecommendationService;
import com.example.security.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@WebMvcTest(RecommendationController.class)
@Import(SecurityConfig.class)
@WithMockUser(username = "user123")
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecommendationService recommendationService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final UUID uuid1 = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void testGetRecommendations() throws Exception {
        Movie m1 = new Movie(uuid1, "Inception", "description", 2010, Genre.SCI_FI);
        Mockito.when(recommendationService.getRecommendations("user123", "SCI_FI", 2000, 2020))
                .thenReturn(Arrays.asList(m1));

        mockMvc.perform(get("/api/recommendations")
                        .param("genre", "SCI_FI")
                        .param("releaseYearFrom", "2000")
                        .param("releaseYearTo", "2020"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Inception")));

        Mockito.verify(recommendationService).getRecommendations("user123", "SCI_FI", 2000, 2020);
    }

    @Test
    void testShareRecommendation() throws Exception {
        Map<String, String> payload = Map.of(
                "movieId", uuid1.toString(),
                "recipientId", "recipient123"
        );

        mockMvc.perform(post("/api/recommendations/share")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Movie recommendation shared successfully")));

        Mockito.verify(recommendationService).shareMovie("user123", "recipient123", uuid1.toString());
    }

    @Test
    void testGetSharedRecommendations() throws Exception {
        Movie m1 = new Movie(uuid1, "Inception", "description", 2010, Genre.SCI_FI);
        SharedRecommendationResponse res = new SharedRecommendationResponse(
                m1,
                "friendUsername",
                "friendId",
                "2026-06-04T12:00:00"
        );

        Mockito.when(recommendationService.getSharedRecommendations("user123"))
                .thenReturn(Arrays.asList(res));

        mockMvc.perform(get("/api/recommendations/shared"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].movie.title", is("Inception")))
                .andExpect(jsonPath("$[0].sharedByUsername", is("friendUsername")));

        Mockito.verify(recommendationService).getSharedRecommendations("user123");
    }
}
