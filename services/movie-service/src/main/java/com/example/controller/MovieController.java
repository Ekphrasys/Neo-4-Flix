package com.example.controller;

import com.example.model.Genre;
import com.example.model.Movie;
import com.example.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    @Autowired
    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public ResponseEntity<List<Movie>> getAllMovies(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "genre", required = false) Genre genre,
            @RequestParam(value = "releaseYearFrom", required = false) Integer releaseYearFrom,
            @RequestParam(value = "releaseYearTo", required = false) Integer releaseYearTo
    ) {
        q = normalizeBlank(q);
        title = normalizeBlank(title);

        // Validation minimaliste des bornes
        if (releaseYearFrom != null && releaseYearTo != null && releaseYearFrom > releaseYearTo) {
            return ResponseEntity.badRequest().build();
        }

        boolean hasFilters = q != null || title != null || genre != null || releaseYearFrom != null || releaseYearTo != null;
        if (!hasFilters) {
            return ResponseEntity.ok(movieService.getAllMovies());
        }

        return ResponseEntity.ok(movieService.searchMovies(q, title, genre, releaseYearFrom, releaseYearTo));
    }

    private static String normalizeBlank(String s) {
        if (s == null) return null;
        String trimmed = s.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Movie> getMovieById(@PathVariable UUID id) {
        return movieService.getMovieById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Movie> createMovie(@RequestBody Movie movie) {
        return ResponseEntity.ok(movieService.saveMovie(movie));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Movie> updateMovie(@PathVariable UUID id, @RequestBody Movie movie) {
        return movieService.getMovieById(id)
                .map(existingMovie -> {
                    movie.setId(id);
                    return ResponseEntity.ok(movieService.saveMovie(movie));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable UUID id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/recommendations/{userId}")
    public ResponseEntity<List<Movie>> getRecommendations(@PathVariable String userId) {
        return ResponseEntity.ok(movieService.getRecommendations(userId));
    }
}