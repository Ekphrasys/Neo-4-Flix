package com.example.repository;

import com.example.model.Rating;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.List;

public interface RatingRepository extends Neo4jRepository<Rating, Long> {

    List<Rating> findByRating(int rating);

    List<Rating> findByMovieId(Long movieId);
}