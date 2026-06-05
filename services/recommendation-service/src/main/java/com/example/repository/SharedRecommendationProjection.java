package com.example.repository;

import com.example.model.Movie;

public class SharedRecommendationProjection {

    private Movie movie;
    private String sharedByUsername;
    private String sharedByUserId;
    private String sharedAt;

    public SharedRecommendationProjection() {}

    public Movie getMovie() { return movie; }
    public void setMovie(Movie movie) { this.movie = movie; }

    public String getSharedByUsername() { return sharedByUsername; }
    public void setSharedByUsername(String sharedByUsername) { this.sharedByUsername = sharedByUsername; }

    public String getSharedByUserId() { return sharedByUserId; }
    public void setSharedByUserId(String sharedByUserId) { this.sharedByUserId = sharedByUserId; }

    public String getSharedAt() { return sharedAt; }
    public void setSharedAt(String sharedAt) { this.sharedAt = sharedAt; }
}
