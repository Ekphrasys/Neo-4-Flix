package com.example.repository;

import com.example.model.Movie;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WatchlistRepository extends Neo4jRepository<Movie, UUID> {

    @Query(
            "MATCH (u:User {id: $userId})-[:WATCHLIST]->(m:Movie) " +
            "RETURN m " +
            "ORDER BY m.releaseYear DESC, m.title ASC"
    )
    List<Movie> getWatchlist(@Param("userId") String userId);

    @Query(
            "MATCH (u:User {id: $userId}) " +
            "MATCH (m:Movie {id: $movieId}) " +
            "MERGE (u)-[:WATCHLIST]->(m)"
    )
    void addToWatchlist(@Param("userId") String userId, @Param("movieId") UUID movieId);

    @Query(
            "MATCH (u:User {id: $userId})-[r:WATCHLIST]->(m:Movie {id: $movieId}) " +
            "DELETE r"
    )
    void removeFromWatchlist(@Param("userId") String userId, @Param("movieId") UUID movieId);

    @Query(
            "MATCH (u:User {id: $userId})-[:WATCHLIST]->(m:Movie {id: $movieId}) " +
            "RETURN count(m) > 0"
    )
    boolean existsInWatchlist(@Param("userId") String userId, @Param("movieId") UUID movieId);
}

