package com.example.controller;

import com.example.model.Movie;
import com.example.model.Rating;
import com.example.repository.RatingRepository;
import com.example.repository.RatingStats;
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
import java.util.UUID;

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
    private static final UUID uuid1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID uuid2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID uuid3 = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID MOVIE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000010");

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
        Rating rating = buildRating(uuid1, 5, MOVIE_UUID, "Inception");
        when(ratingRepository.findAll()).thenReturn(List.of(rating));

        mockMvc.perform(get("/api/ratings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(uuid1.toString()))
                .andExpect(jsonPath("$[0].rating").value(5))
                .andExpect(jsonPath("$[0].movie.id").value(MOVIE_UUID.toString()));
    }

    @Test
    void getRatingByIdReturnsNotFoundWhenMissing() throws Exception {
        UUID nonExistentId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        when(ratingRepository.findByUuid(nonExistentId.toString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/ratings/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void createRatingReturnsCreated() throws Exception {
        UUID uuid4 = UUID.fromString("00000000-0000-0000-0000-000000000004");
        UUID movieUuid = UUID.fromString("00000000-0000-0000-0000-000000000200");
        Rating payload = buildRating(null, 4, movieUuid, null);
        Rating saved = buildRating(uuid4, 4, movieUuid, null);
        when(ratingRepository.save(any(Rating.class))).thenReturn(saved);

        mockMvc.perform(post("/api/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(uuid4.toString()))
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.movie.id").value(movieUuid.toString()));
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
        UUID movieUuid1 = UUID.fromString("00000000-0000-0000-0000-000000000300");
        UUID movieUuid2 = UUID.fromString("00000000-0000-0000-0000-000000000301");
        Rating existing = buildRating(uuid3, 2, movieUuid1, null);
        Rating payload = buildRating(null, 5, movieUuid2, null);
        Rating updated = buildRating(uuid3, 5, movieUuid2, null);
        when(ratingRepository.findByUuid(uuid3.toString())).thenReturn(Optional.of(existing));
        when(ratingRepository.save(any(Rating.class))).thenReturn(updated);

        mockMvc.perform(put("/api/ratings/{id}", uuid3)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(uuid3.toString()))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.movie.id").value(movieUuid2.toString()));
    }

    @Test
    void deleteRatingReturnsNoContentWhenExists() throws Exception {
        Rating existing = buildRating(uuid2, 4, MOVIE_UUID, null);
        when(ratingRepository.findByUuid(uuid2.toString())).thenReturn(Optional.of(existing));
        doNothing().when(ratingRepository).delete(existing);

        mockMvc.perform(delete("/api/ratings/{id}", uuid2))
                .andExpect(status().isNoContent());

        verify(ratingRepository).delete(existing);
    }

    @Test
    void deleteRatingReturnsNotFoundWhenMissing() throws Exception {
        UUID nonExistentId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        when(ratingRepository.findByUuid(nonExistentId.toString())).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/ratings/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Rating not found"));
    }

    @Test
    void getAverageRatingReturnsStats() throws Exception {
        RatingStats mockStats = new RatingStats(4.2, 10L);
        when(ratingRepository.getAverageRating(MOVIE_UUID.toString())).thenReturn(mockStats);

        mockMvc.perform(get("/api/ratings/movie/{movieId}/average", MOVIE_UUID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(4.2))
                .andExpect(jsonPath("$.totalRatings").value(10));
    }

    private Rating buildRating(UUID id, int ratingValue, UUID movieId, String title) {
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
