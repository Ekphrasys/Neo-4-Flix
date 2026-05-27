package com.example.repository;

import com.example.model.Rating;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.List;
import java.util.UUID;

public interface RatingRepository extends Neo4jRepository<Rating, UUID> {

    List<Rating> findByRating(int rating);

    List<Rating> findByMovieId(UUID movieId);
}