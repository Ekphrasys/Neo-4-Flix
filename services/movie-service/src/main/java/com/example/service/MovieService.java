package com.example.service;

import com.example.model.Genre;
import com.example.model.Movie;
import com.example.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    @Autowired
    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public List<Movie> searchMovies(String q,
                                   String title,
                                   Genre genre,
                                   Integer releaseYearFrom,
                                   Integer releaseYearTo) {
        if (releaseYearFrom != null && releaseYearTo != null && releaseYearFrom > releaseYearTo) {
            throw new IllegalArgumentException("releaseYearFrom must be <= releaseYearTo");
        }
        return movieRepository.searchMovies(q, title, genre, releaseYearFrom, releaseYearTo);
    }

    public Optional<Movie> getMovieById(Long id) {
        return movieRepository.findById(id);
    }

    public Movie saveMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    public void deleteMovie(Long id) {
        movieRepository.deleteById(id);
    }

    public List<Movie> getRecommendations(String userId) {
        return movieRepository.getRecommendations(userId);
    }
}
