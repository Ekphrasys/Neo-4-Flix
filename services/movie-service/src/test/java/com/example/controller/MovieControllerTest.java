package com.example.controller;

import com.example.model.Movie;
import com.example.model.Genre;
import com.example.service.MovieService;
import com.example.security.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(MovieController.class)
@Import(SecurityConfig.class)
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovieService movieService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAllMovies() throws Exception {
        Movie m1 = new Movie(1L, "Inception", "description", 2020, Genre.ACTION);
        Movie m2 = new Movie(2L, "Interstellar", "description", 2020, Genre.ACTION);

        Mockito.when(movieService.getAllMovies()).thenReturn(Arrays.asList(m1, m2));

        mockMvc.perform(get("/api/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Inception")));
    }

    @Test
    void testGetMovieById_Found() throws Exception {
        Movie movie = new Movie(1L, "Inception", "description", 2020, Genre.ACTION);
        Mockito.when(movieService.getMovieById(1L)).thenReturn(Optional.of(movie));

        mockMvc.perform(get("/api/movies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Inception")));
    }

    @Test
    void testGetMovieById_NotFound() throws Exception {
        Mockito.when(movieService.getMovieById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/movies/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateMovie() throws Exception {
        Movie movie = new Movie(null, "Tenet", "description", 2020, Genre.ACTION);
        Movie savedMovie = new Movie(1L, "Tenet", "description", 2020, Genre.ACTION);

        Mockito.when(movieService.saveMovie(Mockito.any(Movie.class))).thenReturn(savedMovie);

        mockMvc.perform(post("/api/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movie)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }
}