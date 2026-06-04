package com.example.model;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.util.UUID;

@Node("Movie")
@Data
public class Movie {
    @Id @GeneratedValue
    private UUID id;

    private String title;
    private String description;
    private int releaseYear;
    private Genre genre;

    public Movie() {
    }

    public Movie(UUID id, String title, String description, int releaseYear, Genre genre) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.releaseYear = releaseYear;
        this.genre = genre;
    }
}
