package com.example.service;

import com.example.model.Movie;
import com.example.repository.MovieRepository;
import com.example.repository.WatchlistRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class WatchlistService {

	private final WatchlistRepository watchlistRepository;
	private final MovieRepository movieRepository;

	public WatchlistService(WatchlistRepository watchlistRepository, MovieRepository movieRepository) {
		this.watchlistRepository = watchlistRepository;
		this.movieRepository = movieRepository;
	}

	public List<Movie> getWatchlist(String userId) {
		requireUserId(userId);
		return watchlistRepository.getWatchlist(userId);
	}

	public void addToWatchlist(String userId, Long movieId) {
		requireUserId(userId);
		requireMovieId(movieId);

		if (!movieRepository.existsById(movieId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found");
		}

		// Idempotent grâce au MERGE côté Cypher
		watchlistRepository.addToWatchlist(userId, movieId);
	}

	public void removeFromWatchlist(String userId, Long movieId) {
		requireUserId(userId);
		requireMovieId(movieId);

		if (!movieRepository.existsById(movieId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found");
		}

		watchlistRepository.removeFromWatchlist(userId, movieId);
	}

	public boolean existsInWatchlist(String userId, Long movieId) {
		requireUserId(userId);
		requireMovieId(movieId);
		return watchlistRepository.existsInWatchlist(userId, movieId);
	}

	private static void requireUserId(String userId) {
		if (userId == null || userId.isBlank()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing user");
		}
	}

	private static void requireMovieId(Long movieId) {
		if (movieId == null || movieId < 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid movie id");
		}
	}
}


