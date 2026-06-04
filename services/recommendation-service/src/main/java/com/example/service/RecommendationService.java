package com.example.service;

import com.example.dto.SharedRecommendationResponse;
import com.example.model.Movie;
import com.example.repository.RecommendationRepository;
import com.example.repository.SharedRecommendationProjection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;

    @Autowired
    public RecommendationService(RecommendationRepository recommendationRepository) {
        this.recommendationRepository = recommendationRepository;
    }

    public List<Movie> getRecommendations(String userId, String genre, Integer releaseYearFrom, Integer releaseYearTo) {
        // Enforce that empty genre query parameter is treated as null
        String genreFilter = (genre != null && !genre.trim().isEmpty()) ? genre.trim() : null;

        List<Movie> collab = recommendationRepository.getCollaborativeRecommendations(userId, genreFilter, releaseYearFrom, releaseYearTo);
        Set<Movie> uniqueMovies = new LinkedHashSet<>(collab);

        if (uniqueMovies.size() < 5) {
            List<Movie> fallback = recommendationRepository.getFallbackRecommendations(userId, genreFilter, releaseYearFrom, releaseYearTo);
            for (Movie m : fallback) {
                uniqueMovies.add(m);
                if (uniqueMovies.size() >= 5) {
                    break;
                }
            }
        }

        return new ArrayList<>(uniqueMovies);
    }

    @Transactional
    public void shareMovie(String senderId, String recipientId, String movieId) {
        recommendationRepository.shareMovie(senderId, recipientId, movieId);
    }

    public List<SharedRecommendationResponse> getSharedRecommendations(String userId) {
        List<SharedRecommendationProjection> projections = recommendationRepository.getSharedRecommendations(userId);
        return projections.stream()
                .map(proj -> new SharedRecommendationResponse(
                        proj.getMovie(),
                        proj.getSharedByUsername(),
                        proj.getSharedByUserId(),
                        proj.getSharedAt()
                ))
                .collect(Collectors.toList());
    }
}
