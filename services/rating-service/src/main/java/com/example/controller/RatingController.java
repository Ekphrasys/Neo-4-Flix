package com.example.controller;

import com.example.model.Movie;
import com.example.model.Rating;
import com.example.repository.RatingRepository;
import com.example.repository.RatingStats;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {
    private final RatingRepository ratingRepository;
    private static final String ERROR_KEY = "error";
    private static final String NOTFOUND_KEY = "Rating not found";

    public RatingController(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    @GetMapping
    public ResponseEntity<List<Rating>> getAllRatings() {
        return ResponseEntity.ok(ratingRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getRatingById(@PathVariable UUID id) {
        Optional<Rating> rating = ratingRepository.findByUuid(id.toString());
        if (rating.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(NOTFOUND_KEY));
        }
        return ResponseEntity.ok(rating.get());
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<Rating>> getRatingsByMovieId(@PathVariable UUID movieId) {
        return ResponseEntity.ok(ratingRepository.findByMovieId(movieId));
    }

    @GetMapping("/value/{value}")
    public ResponseEntity<List<Rating>> getRatingsByValue(@PathVariable int value) {
        return ResponseEntity.ok(ratingRepository.findByRating(value));
    }

    @PostMapping
    public ResponseEntity<Object> createRating(@RequestBody Rating rating) {
        if (rating.getMovie() == null || rating.getMovie().getId() == null) {
            return ResponseEntity.badRequest().body(error("Movie id is required"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ratingRepository.save(rating));
    }

    @PostMapping("/movie/{movieId}")
    public ResponseEntity<Object> createRatingForMovie(@PathVariable UUID movieId, @RequestBody Rating rating) {
        if (rating == null) {
            return ResponseEntity.badRequest().body(error("Rating payload is required"));
        }
        Movie movie = new Movie();
        movie.setId(movieId);
        rating.setMovie(movie);
        return ResponseEntity.status(HttpStatus.CREATED).body(ratingRepository.save(rating));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateRating(@PathVariable UUID id, @RequestBody Rating payload) {
        Optional<Rating> existing = ratingRepository.findByUuid(id.toString());
        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(NOTFOUND_KEY));
        }

        Rating rating = existing.get();
        rating.setRating(payload.getRating());
        if (payload.getMovie() != null) {
            rating.setMovie(payload.getMovie());
        }

        return ResponseEntity.ok(ratingRepository.save(rating));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteRating(@PathVariable UUID id) {
        Optional<Rating> existing = ratingRepository.findByUuid(id.toString());
        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(NOTFOUND_KEY));
        }
        ratingRepository.delete(existing.get());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/movie/{movieId}/user")
    public ResponseEntity<Map<String, Object>> getUserRatingForMovie(@PathVariable UUID movieId, Authentication authentication) {
        String userId = authentication.getName();
        Integer rating = ratingRepository.getUserRatingForMovie(userId, movieId.toString());
        Map<String, Object> response = new HashMap<>();
        response.put("rating", rating);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/movie/{movieId}/user")
    public ResponseEntity<Object> saveRatingForMovie(
            @PathVariable UUID movieId,
            @RequestBody Map<String, Integer> payload,
            Authentication authentication) {
        if (payload == null || !payload.containsKey("rating")) {
            return ResponseEntity.badRequest().body(error("Rating value is required"));
        }
        int rating = payload.get("rating");
        if (rating < 1 || rating > 5) {
            return ResponseEntity.badRequest().body(error("Rating must be between 1 and 5"));
        }
        String userId = authentication.getName();
        if (!ratingRepository.isMovieInWatchlist(userId, movieId.toString())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error("You can only rate movies that are in your watchlist."));
        }
        ratingRepository.saveRating(userId, movieId.toString(), rating);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/movie/{movieId}/user")
    public ResponseEntity<Void> deleteUserRatingForMovie(@PathVariable UUID movieId, Authentication authentication) {
        String userId = authentication.getName();
        ratingRepository.deleteRating(userId, movieId.toString());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/movie/{movieId}/average")
    public ResponseEntity<RatingStats> getAverageRating(@PathVariable UUID movieId) {
        RatingStats stats = ratingRepository.getAverageRating(movieId.toString());
        return ResponseEntity.ok(stats);
    }

    private Map<String, String> error(String message) {
        Map<String, String> body = new HashMap<>();
        body.put(ERROR_KEY, message);
        return body;
    }

}