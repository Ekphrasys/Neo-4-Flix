package com.example.controller;

import com.example.dto.SharedRecommendationResponse;
import com.example.model.Movie;
import com.example.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Autowired
    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public ResponseEntity<List<Movie>> getRecommendations(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Integer releaseYearFrom,
            @RequestParam(required = false) Integer releaseYearTo,
            Authentication authentication) {
        String userId = authentication.getName();
        List<Movie> recommendations = recommendationService.getRecommendations(userId, genre, releaseYearFrom, releaseYearTo);
        return ResponseEntity.ok(recommendations);
    }

    @PostMapping("/share")
    public ResponseEntity<Map<String, String>> shareRecommendation(
            @RequestBody Map<String, String> payload,
            Authentication authentication) {
        if (payload == null || !payload.containsKey("movieId") || !payload.containsKey("recipientId")) {
            return ResponseEntity.badRequest().body(Map.of("error", "movieId and recipientId are required"));
        }

        String senderId = authentication.getName();
        String movieId = payload.get("movieId");
        String recipientId = payload.get("recipientId");

        recommendationService.shareMovie(senderId, recipientId, movieId);
        return ResponseEntity.ok(Map.of("message", "Movie recommendation shared successfully"));
    }

    @GetMapping("/shared")
    public ResponseEntity<List<SharedRecommendationResponse>> getSharedRecommendations(
            Authentication authentication) {
        String userId = authentication.getName();
        List<SharedRecommendationResponse> shared = recommendationService.getSharedRecommendations(userId);
        return ResponseEntity.ok(shared);
    }
}
