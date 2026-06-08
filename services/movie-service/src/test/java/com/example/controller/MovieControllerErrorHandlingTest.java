package com.example.controller;

import com.example.model.Genre;
import com.example.model.Movie;
import com.example.service.MovieService;
import com.example.security.SecurityConfig;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Tests pour la gestion des erreurs et entrées malveillantes dans MovieController
 * Documents le point d'audit: "Does the application appropriately handle errors 
 * coming from unexpected input or malicious use?"
 */
@WebMvcTest(MovieController.class)
@Import(SecurityConfig.class)
@WithMockUser(username = "test-user")
@DisplayName("MovieController - Error Handling & Malicious Input Tests")
class MovieControllerErrorHandlingTest {
    
    private static final UUID uuid1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private MovieService movieService;

    @Autowired
    private org.springframework.web.context.WebApplicationContext wac;
    
    // ==================== GET BY ID - EDGE CASES ====================
    
    @Test
    @DisplayName("Should handle malformed UUID")
    void getMovieByIdWithMalformedUUID() throws Exception {
        mockMvc.perform(get("/api/movies/not-a-uuid"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should handle NULL UUID in path")
    void getMovieByIdWithNullUUID() throws Exception {
        mockMvc.perform(get("/api/movies/null"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should handle extremely long UUID")
    void getMovieByIdWithExtremelyLongUUID() throws Exception {
        String longUUID = "00000000-0000-0000-0000-000000000001" + "x".repeat(1000);
        mockMvc.perform(get("/api/movies/" + longUUID))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should handle SQL injection pattern in UUID")
    void getMovieByIdWithSQLInjection() throws Exception {
        String sqlInjection = "00000000-0000-0000-0000-000000000001' OR 1=1; --";
        mockMvc.perform(get("/api/movies/" + sqlInjection))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should handle non-existent UUID")
    void getMovieByIdNonExistent() throws Exception {
        UUID nonExistentId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        
        Mockito.when(movieService.getMovieById(nonExistentId))
                .thenReturn(Optional.empty());
        
        mockMvc.perform(get("/api/movies/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }
    
    // ==================== SEARCH - MALICIOUS INPUTS ====================
    
    @ParameterizedTest
    @DisplayName("Should handle SQL injection patterns in search queries")
    @ValueSource(strings = {
            "' OR '1'='1",
            "'; DROP TABLE movies; --",
            "1' UNION SELECT * FROM users --",
            "admin' --",
            "\" OR \"\"=\"",
            "1; DELETE FROM movies"
    })
    void searchMoviesWithSQLInjection(String sqlInjection) throws Exception {
        Mockito.when(movieService.searchMovies(eq(sqlInjection), any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        
        mockMvc.perform(get("/api/movies")
                .param("q", sqlInjection))
                .andExpect(status().isOk());
        
        // Verify the service was called with the literal string, not interpreted
        Mockito.verify(movieService).searchMovies(eq(sqlInjection), any(), any(), any(), any());
    }
    
    @Test
    @DisplayName("Should handle XSS attempts in search query")
    void searchMoviesWithXSSAttempt() throws Exception {
        String xssAttempt = "<script>alert('xss')</script>";
        
        Mockito.when(movieService.searchMovies(eq(xssAttempt), any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        
        mockMvc.perform(get("/api/movies")
                .param("q", xssAttempt))
                .andExpect(status().isOk());
        
        Mockito.verify(movieService).searchMovies(eq(xssAttempt), any(), any(), any(), any());
    }
    
    @Test
    @DisplayName("Should handle very long search queries (>10000 chars)")
    void searchMoviesWithExtremelyLongQuery() throws Exception {
        String longQuery = "a".repeat(10000);
        
        Mockito.when(movieService.searchMovies(eq(longQuery), any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        
        mockMvc.perform(get("/api/movies")
                .param("q", longQuery))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("Should handle NULL query parameter")
    void searchMoviesWithNullQuery() throws Exception {
        mockMvc.perform(get("/api/movies")
                .param("q", ""))
                .andExpect(status().isOk());
    }
    
    // ==================== SEARCH - YEAR RANGE VALIDATION ====================
    
    @Test
    @DisplayName("Should reject negative release year")
    void searchMoviesWithNegativeReleaseYear() throws Exception {
        mockMvc.perform(get("/api/movies")
                .param("releaseYearFrom", "-1000"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should reject invalid year range (from > to)")
    void searchMoviesWithInvalidYearRange() throws Exception {
        mockMvc.perform(get("/api/movies")
                .param("releaseYearFrom", "2025")
                .param("releaseYearTo", "1999"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should handle extremely large year values")
    void searchMoviesWithExtremelyLargeYearValues() throws Exception {
        mockMvc.perform(get("/api/movies")
                .param("releaseYearFrom", "999999")
                .param("releaseYearTo", "1000000"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should handle non-integer year values")
    void searchMoviesWithNonIntegerYears() throws Exception {
        mockMvc.perform(get("/api/movies")
                .param("releaseYearFrom", "not-a-number")
                .param("releaseYearTo", "also-not-a-number"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should handle floating point year values")
    void searchMoviesWithFloatYearValues() throws Exception {
        mockMvc.perform(get("/api/movies")
                .param("releaseYearFrom", "2020.5")
                .param("releaseYearTo", "2021.7"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should handle year overflow (beyond Integer.MAX_VALUE)")
    void searchMoviesWithYearOverflow() throws Exception {
        long overflow = (long)Integer.MAX_VALUE + 1;
        mockMvc.perform(get("/api/movies")
                .param("releaseYearFrom", String.valueOf(overflow)))
                .andExpect(status().isBadRequest());
    }
    
    // ==================== CREATE MOVIE - VALIDATION ====================
    
    @Test
    @DisplayName("Should reject movie with NULL title")
    void createMovieWithNullTitle() throws Exception {
        Movie movie = new Movie(null, null, "Description", 2020, Genre.ACTION);
        
        mockMvc.perform(post("/api/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movie)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should reject movie with empty title")
    void createMovieWithEmptyTitle() throws Exception {
        Movie movie = new Movie(null, "", "Description", 2020, Genre.ACTION);
        
        mockMvc.perform(post("/api/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movie)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should handle movie with extremely long title (>10000 chars)")
    void createMovieWithExtremelyLongTitle() throws Exception {
        String longTitle = "a".repeat(10000);
        Movie movie = new Movie(null, longTitle, "Description", 2020, Genre.ACTION);
        
        mockMvc.perform(post("/api/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movie)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should handle movie with negative release year")
    void createMovieWithNegativeReleaseYear() throws Exception {
        Movie movie = new Movie(null, "Test Movie", "Description", -1000, Genre.ACTION);
        
        mockMvc.perform(post("/api/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movie)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should handle movie with future release year")
    void createMovieWithFutureReleaseYear() throws Exception {
        Movie movie = new Movie(null, "Test Movie", "Description", 9999, Genre.ACTION);
        
        mockMvc.perform(post("/api/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movie)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should handle movie with XSS in title")
    void createMovieWithXSSInTitle() throws Exception {
        String xssTitle = "<img src=x onerror=alert('xss')>";
        Movie movie = new Movie(null, xssTitle, "Description", 2020, Genre.ACTION);
        
        UUID savedId = uuid1;
        Movie savedMovie = new Movie(savedId, xssTitle, "Description", 2020, Genre.ACTION);
        Mockito.when(movieService.saveMovie(any()))
                .thenReturn(savedMovie);
        
        mockMvc.perform(post("/api/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movie)))
                .andExpect(status().isOk());
        
        // XSS string should be safely stored and returned
        Mockito.verify(movieService).saveMovie(any());
    }
    
    @Test
    @DisplayName("Should handle movie with SQL injection in description")
    void createMovieWithSQLInDescription() throws Exception {
        String sqlDesc = "'; DROP TABLE movies; --";
        Movie movie = new Movie(null, "Test", sqlDesc, 2020, Genre.ACTION);
        
        UUID savedId = uuid1;
        Movie savedMovie = new Movie(savedId, "Test", sqlDesc, 2020, Genre.ACTION);
        Mockito.when(movieService.saveMovie(any()))
                .thenReturn(savedMovie);
        
        mockMvc.perform(post("/api/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movie)))
                .andExpect(status().isOk());
        
        Mockito.verify(movieService).saveMovie(any());
    }
    
    @Test
    @DisplayName("Should handle malformed JSON in POST")
    void createMovieWithMalformedJSON() throws Exception {
        String malformedJson = "{\"title\": \"Test\", \"description\": \"Desc\"";  // Missing }
        
        mockMvc.perform(post("/api/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should handle POST with NULL body")
    void createMovieWithNullBody() throws Exception {
        mockMvc.perform(post("/api/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content("null"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should handle POST with empty body")
    void createMovieWithEmptyBody() throws Exception {
        mockMvc.perform(post("/api/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }
    
    // ==================== UPDATE MOVIE - VALIDATION ====================
    
    @Test
    @DisplayName("Should handle update non-existent movie")
    void updateNonExistentMovie() throws Exception {
        UUID nonExistentId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        Movie movie = new Movie(null, "Updated", "Description", 2020, Genre.ACTION);
        
        Mockito.when(movieService.getMovieById(nonExistentId))
                .thenReturn(Optional.empty());
        
        mockMvc.perform(put("/api/movies/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movie)))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("Should handle update with malformed UUID")
    void updateMovieWithMalformedUUID() throws Exception {
        Movie movie = new Movie(null, "Updated", "Description", 2020, Genre.ACTION);
        
        mockMvc.perform(put("/api/movies/not-a-uuid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movie)))
                .andExpect(status().isBadRequest());
    }
    
    // ==================== DELETE MOVIE ====================
    
    @Test
    @DisplayName("Should handle delete with malformed UUID")
    void deleteMovieWithMalformedUUID() throws Exception {
        mockMvc.perform(delete("/api/movies/not-a-uuid"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should handle delete non-existent movie gracefully")
    void deleteNonExistentMovie() throws Exception {
        UUID nonExistentId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        
        Mockito.doNothing().when(movieService).deleteMovie(nonExistentId);
        
        mockMvc.perform(delete("/api/movies/{id}", nonExistentId))
                .andExpect(status().isNoContent());
    }
    
    // ==================== RECOMMENDATIONS ====================
    
    @Test
    @DisplayName("Should handle recommendations with SQL injection in userId")
    void getRecommendationsWithSQLInjection() throws Exception {
        String sqlInjection = "userId' OR '1'='1";
        
        Mockito.when(movieService.getRecommendations(sqlInjection))
                .thenReturn(Collections.emptyList());
        
        mockMvc.perform(get("/api/movies/recommendations/{userId}", sqlInjection))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("Should handle recommendations with extremely long userId")
    void getRecommendationsWithExtremelyLongUserId() throws Exception {
        String longUserId = "a".repeat(10000);
        
        Mockito.when(movieService.getRecommendations(longUserId))
                .thenReturn(Collections.emptyList());
        
        mockMvc.perform(get("/api/movies/recommendations/{userId}", longUserId))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("Should handle recommendations with NULL userId")
    void getRecommendationsWithNullUserId() throws Exception {
        mockMvc.perform(get("/api/movies/recommendations/null"))
                .andExpect(status().isOk());  // Depends on implementation
    }
}

