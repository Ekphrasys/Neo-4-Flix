package com.example.repository;

import com.example.model.Movie;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WatchlistRepository extends Neo4jRepository<Movie, Long> {

    @Query(
            "MATCH (u:User {userId: $userId})-[:WATCHLIST]->(m:Movie) " +
            "RETURN m " +
            "ORDER BY m.releaseYear DESC, m.title ASC"
    )
    List<Movie> getWatchlist(@Param("userId") String userId);

    @Query(
            "MATCH (m:Movie) " +
            "WHERE id(m) = $movieId " +
            "MERGE (u:User {userId: $userId}) " +
            "MERGE (u)-[:WATCHLIST]->(m)"
    )
    void addToWatchlist(@Param("userId") String userId, @Param("movieId") Long movieId);

    @Query(
            "MATCH (u:User {userId: $userId})-[r:WATCHLIST]->(m:Movie) " +
            "WHERE id(m) = $movieId " +
            "DELETE r"
    )
    void removeFromWatchlist(@Param("userId") String userId, @Param("movieId") Long movieId);

    @Query(
            "MATCH (u:User {userId: $userId})-[:WATCHLIST]->(m:Movie) " +
            "WHERE id(m) = $movieId " +
            "RETURN count(m) > 0"
    )
    boolean existsInWatchlist(@Param("userId") String userId, @Param("movieId") Long movieId);
}

