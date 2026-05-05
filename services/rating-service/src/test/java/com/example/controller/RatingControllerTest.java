package com.example.controller;

import com.example.model.Movie;
import com.example.model.Rating;
import com.example.repository.RatingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RatingController.class)
@AutoConfigureMockMvc(addFilters = false)
class RatingControllerTest {

    @SpringBootApplication
    static class TestApplication {
        // Minimal boot config so @WebMvcTest can start in this module.
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RatingRepository ratingRepository;

    @Test
    void getAllRatingsReturnsList() throws Exception {
        Rating rating = buildRating(1L, 5, 100L, "Inception");
        when(ratingRepository.findAll()).thenReturn(List.of(rating));

        mockMvc.perform(get("/api/ratings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].rating").value(5))
                .andExpect(jsonPath("$[0].movie.id").value(100));
    }

    @Test
    void getRatingByIdReturnsNotFoundWhenMissing() throws Exception {
        when(ratingRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/ratings/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Rating not found"));
    }

    @Test
    void createRatingReturnsCreated() throws Exception {
        Rating payload = buildRating(null, 4, 200L, null);
        Rating saved = buildRating(10L, 4, 200L, null);
        when(ratingRepository.save(any(Rating.class))).thenReturn(saved);

        mockMvc.perform(post("/api/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.movie.id").value(200));
    }

    @Test
    void createRatingReturnsBadRequestWhenMovieIdMissing() throws Exception {
        Rating payload = new Rating();
        payload.setRating(3);
        payload.setMovie(new Movie());

        mockMvc.perform(post("/api/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Movie id is required"));
    }

    @Test
    void updateRatingReturnsUpdatedEntity() throws Exception {
        Rating existing = buildRating(7L, 2, 300L, null);
        Rating payload = buildRating(null, 5, 301L, null);
        Rating updated = buildRating(7L, 5, 301L, null);
        when(ratingRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(ratingRepository.save(any(Rating.class))).thenReturn(updated);

        mockMvc.perform(put("/api/ratings/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.movie.id").value(301));
    }

    @Test
    void deleteRatingReturnsNoContentWhenExists() throws Exception {
        when(ratingRepository.existsById(5L)).thenReturn(true);
        doNothing().when(ratingRepository).deleteById(5L);

        mockMvc.perform(delete("/api/ratings/5"))
                .andExpect(status().isNoContent());

        verify(ratingRepository).deleteById(5L);
    }

    @Test
    void deleteRatingReturnsNotFoundWhenMissing() throws Exception {
        when(ratingRepository.existsById(404L)).thenReturn(false);

        mockMvc.perform(delete("/api/ratings/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Rating not found"));
    }

    private Rating buildRating(Long id, int ratingValue, Long movieId, String title) {
        Movie movie = new Movie();
        movie.setId(movieId);
        movie.setTitle(title);

        Rating rating = new Rating();
        rating.setId(id);
        rating.setRating(ratingValue);
        rating.setMovie(movie);
        return rating;
    }
}
