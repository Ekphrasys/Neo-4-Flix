package com.example.repository;

import com.example.model.Rating;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import java.util.Optional;

public interface RatingRepository extends Neo4jRepository<Rating, Long> {

    List<Rating> findByRating(int rating);

    List<Rating> findByMovieId(UUID movieId);

    @Query("MATCH (u)-[r:RATED]->(m) WHERE r.id = $id RETURN r")
    Optional<Rating> findByUuid(@Param("id") String id);

    @Query("MATCH (u:User {id: $userId})-[r:RATED]->(m:Movie {id: $movieId}) RETURN r.rating")
    Integer getUserRatingForMovie(@Param("userId") String userId, @Param("movieId") String movieId);

    @Query("MATCH (u:User {id: $userId})-[:WATCHLIST]->(m:Movie {id: $movieId}) RETURN count(m) > 0")
    boolean isMovieInWatchlist(@Param("userId") String userId, @Param("movieId") String movieId);

    @Transactional
    @Query("MATCH (u:User {id: $userId}) " +
           "MATCH (m:Movie {id: $movieId}) " +
           "MERGE (u)-[r:RATED]->(m) " +
           "SET r.rating = $rating")
    void saveRating(@Param("userId") String userId, @Param("movieId") String movieId, @Param("rating") int rating);

    @Transactional
    @Query("MATCH (u:User {id: $userId})-[r:RATED]->(m:Movie {id: $movieId}) DELETE r")
    void deleteRating(@Param("userId") String userId, @Param("movieId") String movieId);

    @Query("MATCH (m:Movie {id: $movieId})<-[r:RATED]-() " +
           "RETURN coalesce(avg(toInteger(r.rating)), 0.0) AS averageRating, count(r) AS totalRatings")
    RatingStats getAverageRating(@Param("movieId") String movieId);
}