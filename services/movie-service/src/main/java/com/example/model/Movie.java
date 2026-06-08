package com.example.model;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.util.UUID;

@Node("Movie")
@Data
public class Movie {
    @Id @GeneratedValue
    private UUID id;

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 255, message = "Title too long")
    private String title;
    private String description;
    @Min(value = 1888, message = "Year must be valid")
    @Max(value = 2026, message = "Year must be valid")
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}