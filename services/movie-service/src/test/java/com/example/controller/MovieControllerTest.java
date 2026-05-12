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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(MovieController.class)
@Import(SecurityConfig.class)
@WithMockUser(username = "test-user")
class MovieControllerTest {
    private static String TITLE = "Inception";
    private static String DESC = "description";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovieService movieService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAllMovies() throws Exception {
        Movie m1 = new Movie(1L, TITLE, DESC, 2020, Genre.ACTION);
        Movie m2 = new Movie(2L, "Interstellar", DESC, 2020, Genre.ACTION);

        Mockito.when(movieService.getAllMovies()).thenReturn(Arrays.asList(m1, m2));

        mockMvc.perform(get("/api/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is(TITLE)));

        Mockito.verify(movieService).getAllMovies();
        Mockito.verify(movieService, Mockito.never()).searchMovies(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void testSearchMoviesWithQueryParams() throws Exception {
        Movie m1 = new Movie(1L, TITLE, DESC, 2020, Genre.ACTION);

        Mockito.when(movieService.searchMovies(anyString(), Mockito.isNull(), Mockito.isNull(), Mockito.isNull(), Mockito.isNull()))
                .thenReturn(Arrays.asList(m1));

        mockMvc.perform(get("/api/movies").param("q", "Inception"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is(TITLE)));

        Mockito.verify(movieService, Mockito.never()).getAllMovies();
        Mockito.verify(movieService).searchMovies(Mockito.eq("Inception"), Mockito.isNull(), Mockito.isNull(), Mockito.isNull(), Mockito.isNull());
    }

    @Test
    void testSearchMoviesInvalidYearRangeReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/movies")
                        .param("releaseYearFrom", "2025")
                        .param("releaseYearTo", "1999"))
                .andExpect(status().isBadRequest());

        Mockito.verify(movieService, Mockito.never()).getAllMovies();
        Mockito.verify(movieService, Mockito.never()).searchMovies(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void testGetMovieById() throws Exception {
        Movie movie = new Movie(1L, TITLE, DESC, 2020, Genre.ACTION);
        Mockito.when(movieService.getMovieById(1L)).thenReturn(Optional.of(movie));

        mockMvc.perform(get("/api/movies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(TITLE)));
    }

    @Test
    void testGetMovieByIdNotFound() throws Exception {
        Mockito.when(movieService.getMovieById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/movies/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateMovie() throws Exception {
        Movie movie = new Movie(null, "Tenet", DESC, 2020, Genre.ACTION);
        Movie savedMovie = new Movie(1L, "Tenet", DESC, 2020, Genre.ACTION);

        Mockito.when(movieService.saveMovie(any(Movie.class))).thenReturn(savedMovie);

        mockMvc.perform(post("/api/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movie)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }
}