package com.example.controller;

import com.example.model.Movie;
import com.example.model.Rating;
import com.example.repository.RatingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/api/ratings")
public class RatingController {
    private final RatingRepository ratingRepository;
    private static final String ERROR_KEY = "error";

    public RatingController(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    @GetMapping
    public ResponseEntity<List<Rating>> getAllRatings() {
        return ResponseEntity.ok(ratingRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRatingById(@PathVariable Long id) {
        Optional<Rating> rating = ratingRepository.findById(id);
        if (rating.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error("Rating not found"));
        }
        return ResponseEntity.ok(rating.get());
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<Rating>> getRatingsByMovieId(@PathVariable Long movieId) {
        return ResponseEntity.ok(ratingRepository.findByMovieId(movieId));
    }

    @GetMapping("/value/{value}")
    public ResponseEntity<List<Rating>> getRatingsByValue(@PathVariable int value) {
        return ResponseEntity.ok(ratingRepository.findByRating(value));
    }

    @PostMapping
    public ResponseEntity<?> createRating(@RequestBody Rating rating) {
        if (rating.getMovie() == null || rating.getMovie().getId() == null) {
            return ResponseEntity.badRequest().body(error("Movie id is required"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ratingRepository.save(rating));
    }

    @PostMapping("/movie/{movieId}")
    public ResponseEntity<?> createRatingForMovie(@PathVariable Long movieId, @RequestBody Rating rating) {
        if (rating == null) {
            return ResponseEntity.badRequest().body(error("Rating payload is required"));
        }
        Movie movie = new Movie();
        movie.setId(movieId);
        rating.setMovie(movie);
        return ResponseEntity.status(HttpStatus.CREATED).body(ratingRepository.save(rating));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRating(@PathVariable Long id, @RequestBody Rating payload) {
        Optional<Rating> existing = ratingRepository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error("Rating not found"));
        }

        Rating rating = existing.get();
        rating.setRating(payload.getRating());
        if (payload.getMovie() != null) {
            rating.setMovie(payload.getMovie());
        }

        return ResponseEntity.ok(ratingRepository.save(rating));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRating(@PathVariable Long id) {
        if (!ratingRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error("Rating not found"));
        }
        ratingRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private Map<String, String> error(String message) {
        Map<String, String> body = new HashMap<>();
        body.put(ERROR_KEY, message);
        return body;
    }

}