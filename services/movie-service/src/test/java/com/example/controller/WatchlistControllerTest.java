package com.example.controller;

import com.example.model.Genre;
import com.example.model.Movie;
import com.example.security.SecurityConfig;
import com.example.service.WatchlistService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WatchlistController.class)
@Import(SecurityConfig.class)
@WithMockUser(username = "user-123")
class WatchlistControllerTest {
    private static final UUID uuid1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID uuid2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID MOVIE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID ANOTHER_MOVIE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000099");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WatchlistService watchlistService;

    @Test
    void getWatchlist_returnsMovies() throws Exception {
        Movie m1 = new Movie(uuid1, "Inception", "desc", 2010, Genre.SCI_FI);
        Movie m2 = new Movie(uuid2, "Interstellar", "desc", 2014, Genre.SCI_FI);
        Mockito.when(watchlistService.getWatchlist("user-123")).thenReturn(List.of(m1, m2));

        mockMvc.perform(get("/api/watchlist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Inception")));
    }

    @Test
    void addToWatchlist_returnsNoContent() throws Exception {
        mockMvc.perform(post("/api/watchlist/{movieId}", MOVIE_UUID))
                .andExpect(status().isNoContent());
        Mockito.verify(watchlistService).addToWatchlist("user-123", MOVIE_UUID);
    }

    @Test
    void removeFromWatchlist_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/watchlist/{movieId}", MOVIE_UUID))
                .andExpect(status().isNoContent());
        Mockito.verify(watchlistService).removeFromWatchlist("user-123", MOVIE_UUID);
    }

    @Test
    void exists_returnsBoolean() throws Exception {
        Mockito.when(watchlistService.existsInWatchlist("user-123", ANOTHER_MOVIE_UUID)).thenReturn(true);

        mockMvc.perform(get("/api/watchlist/{movieId}/exists", ANOTHER_MOVIE_UUID))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}

