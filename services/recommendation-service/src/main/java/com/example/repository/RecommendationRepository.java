package com.example.repository;

import com.example.model.Movie;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecommendationRepository extends Neo4jRepository<Movie, UUID> {

    @Query("MATCH (u:User {id: $userId}) " +
           "OPTIONAL MATCH (u)-[r:RATED]->(m:Movie) " +
           "WITH u, " +
           "     collect(DISTINCT case when r.rating >= 4 then m.genre END) as likedGenres, " +
           "     collect(DISTINCT case when r.rating <= 2 then m.genre END) as dislikedGenres " +
           "MATCH (rec:Movie) " +
           "WHERE NOT (u)-[:RATED]->(rec) " +
           "  AND (size(likedGenres) = 0 OR rec.genre IN likedGenres) " +
           "  AND NOT rec.genre IN dislikedGenres " +
           "  AND ($genre IS NULL OR rec.genre = $genre) " +
           "  AND ($releaseYearFrom IS NULL OR rec.releaseYear >= $releaseYearFrom) " +
           "  AND ($releaseYearTo IS NULL OR rec.releaseYear <= $releaseYearTo) " +
           "OPTIONAL MATCH (rec)<-[r2:RATED]-() " +
           "WITH rec, avg(r2.rating) as avgRating, count(r2) as ratingCount " +
           "RETURN rec " +
           "ORDER BY coalesce(avgRating, 0.0) DESC, ratingCount DESC, rec.releaseYear DESC " +
           "LIMIT 10")
    List<Movie> getCollaborativeRecommendations(
            @Param("userId") String userId,
            @Param("genre") String genre,
            @Param("releaseYearFrom") Integer releaseYearFrom,
            @Param("releaseYearTo") Integer releaseYearTo
    );


    // Get recommendations even if the user has no ratings, based on movies with the same genre and release year 
    // This is a fallback method for users with no ratings
    @Query("MATCH (u:User {id: $userId}) " +
           "OPTIONAL MATCH (u)-[r:RATED]->(m:Movie) " +
           "WITH u, " +
           "     collect(DISTINCT case when r.rating <= 2 then m.genre END) as dislikedGenres " +
           "MATCH (rec:Movie) " +
           "WHERE NOT (u)-[:RATED]->(rec) " +
           "  AND NOT rec.genre IN dislikedGenres " +
           "  AND ($genre IS NULL OR rec.genre = $genre) " +
           "  AND ($releaseYearFrom IS NULL OR rec.releaseYear >= $releaseYearFrom) " +
           "  AND ($releaseYearTo IS NULL OR rec.releaseYear <= $releaseYearTo) " +
           "OPTIONAL MATCH (rec)<-[r2:RATED]-() " +
           "WITH rec, avg(r2.rating) as avgRating, count(r2) as ratingCount " +
           "RETURN rec " +
           "ORDER BY coalesce(avgRating, 0.0) DESC, ratingCount DESC, rec.releaseYear DESC " +
           "LIMIT 10")
    List<Movie> getFallbackRecommendations(
            @Param("userId") String userId,
            @Param("genre") String genre,
            @Param("releaseYearFrom") Integer releaseYearFrom,
            @Param("releaseYearTo") Integer releaseYearTo
    );

    @Query("MATCH (sender:User {id: $senderId})-[f:FOLLOWS]->(recipient:User {id: $recipientId}) " +
           "MATCH (movie:Movie {id: $movieId}) " +
           "MERGE (sender)-[s:SHARED {movieId: $movieId}]->(recipient) " +
           "SET s.sharedAt = toString(datetime())")
    void shareMovie(
            @Param("senderId") String senderId,
            @Param("recipientId") String recipientId,
            @Param("movieId") String movieId
    );

    @Query("MATCH (sender:User)-[r:SHARED]->(recipient:User {id: $userId}) " +
           "MATCH (movie:Movie {id: r.movieId}) " +
           "RETURN movie, sender.username AS sharedByUsername, sender.id AS sharedByUserId, r.sharedAt AS sharedAt " +
           "ORDER BY r.sharedAt DESC")
    List<SharedRecommendationProjection> getSharedRecommendations(@Param("userId") String userId);
}
