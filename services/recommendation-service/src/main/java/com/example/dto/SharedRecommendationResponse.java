package com.example.dto;

import com.example.model.Movie;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SharedRecommendationResponse {
    private Movie movie;
    private String sharedByUsername;
    private String sharedByUserId;
    private String sharedAt;
}
