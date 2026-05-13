package com.example.controller;

import com.example.model.Movie;
import com.example.service.WatchlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/watchlist")
public class WatchlistController {

    private final WatchlistService watchlistService;

    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    @GetMapping
    public ResponseEntity<List<Movie>> getMyWatchlist(Authentication authentication) {
        String userId = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(watchlistService.getWatchlist(userId));
    }

    @PostMapping("/{movieId}")
    public ResponseEntity<Void> addToWatchlist(@PathVariable Long movieId, Authentication authentication) {
        String userId = authentication != null ? authentication.getName() : null;
        watchlistService.addToWatchlist(userId, movieId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{movieId}")
    public ResponseEntity<Void> removeFromWatchlist(@PathVariable Long movieId, Authentication authentication) {
        String userId = authentication != null ? authentication.getName() : null;
        watchlistService.removeFromWatchlist(userId, movieId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{movieId}/exists")
    public ResponseEntity<Boolean> exists(@PathVariable Long movieId, Authentication authentication) {
        String userId = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(watchlistService.existsInWatchlist(userId, movieId));
    }
}

