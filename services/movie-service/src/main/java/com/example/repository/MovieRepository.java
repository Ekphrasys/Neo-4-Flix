package com.example.repository;

import com.example.model.Genre;
import com.example.model.Movie;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends Neo4jRepository<Movie, Long> {
    List<Movie> findByTitle(String title);

    @Query(
            "MATCH (m:Movie) " +
            "WHERE " +
            "  ($q IS NULL OR (" +
            "    toLower(coalesce(m.title, '')) CONTAINS toLower($q)" +
            "  )) " +
            "  AND ($title IS NULL OR toLower(coalesce(m.title, '')) CONTAINS toLower($title)) " +
            "  AND ($genre IS NULL OR m.genre = $genre) " +
            "  AND ($releaseYearFrom IS NULL OR m.releaseYear >= $releaseYearFrom) " +
            "  AND ($releaseYearTo IS NULL OR m.releaseYear <= $releaseYearTo) " +
            "RETURN m " +
            "ORDER BY m.releaseYear DESC, m.title ASC"
    )
    List<Movie> searchMovies(
            @Param("q") String q,
            @Param("title") String title,
            @Param("genre") Genre genre,
            @Param("releaseYearFrom") Integer releaseYearFrom,
            @Param("releaseYearTo") Integer releaseYearTo
    );

    @Query("MATCH (u:User {userId: $userId})-[r:RATED]->(m:Movie)<-[r2:RATED]-(other:User)-[r3:RATED]->(rec:Movie) " +
           "WHERE NOT (u)-[:RATED]->(rec) " +
           "WITH rec, count(other) as commonUsers, avg(r3.rating) as avgRating " +
           "RETURN rec " +
           "ORDER BY commonUsers DESC, avgRating DESC " +
           "LIMIT 10")
    List<Movie> getRecommendations(@Param("userId") String userId);
}