package com.example.repository;

public class RatingStats {
    private final Double averageRating;
    private final Long totalRatings;

    public RatingStats(Double averageRating, Long totalRatings) {
        this.averageRating = averageRating;
        this.totalRatings = totalRatings;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public Long getTotalRatings() {
        return totalRatings;
    }
}
