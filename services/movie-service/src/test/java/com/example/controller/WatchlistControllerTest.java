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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WatchlistController.class)
@Import(SecurityConfig.class)
@WithMockUser(username = "user-123")
class WatchlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WatchlistService watchlistService;

    @Test
    void getWatchlist_returnsMovies() throws Exception {
        Movie m1 = new Movie(1L, "Inception", "desc", 2010, Genre.SCI_FI);
        Movie m2 = new Movie(2L, "Interstellar", "desc", 2014, Genre.SCI_FI);
        Mockito.when(watchlistService.getWatchlist("user-123")).thenReturn(List.of(m1, m2));

        mockMvc.perform(get("/api/watchlist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Inception")));
    }

    @Test
    void addToWatchlist_returnsNoContent() throws Exception {
        mockMvc.perform(post("/api/watchlist/10"))
                .andExpect(status().isNoContent());
        Mockito.verify(watchlistService).addToWatchlist("user-123", 10L);
    }

    @Test
    void removeFromWatchlist_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/watchlist/10"))
                .andExpect(status().isNoContent());
        Mockito.verify(watchlistService).removeFromWatchlist("user-123", 10L);
    }

    @Test
    void exists_returnsBoolean() throws Exception {
        Mockito.when(watchlistService.existsInWatchlist("user-123", 99L)).thenReturn(true);

        mockMvc.perform(get("/api/watchlist/99/exists"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}

