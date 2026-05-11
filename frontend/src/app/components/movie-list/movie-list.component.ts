import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';

import { MovieService } from '../../services/movie.service';

@Component({
  selector: 'app-movie-list',
  standalone: true,
  imports: [CommonModule],
  template: `
	<section class="page">
	  <header class="page__header">
		<h1>Movies</h1>
	  </header>

	  @if (loading()) {
		<p class="status" aria-live="polite">Loading…</p>
	  } @else if (error()) {
		<p class="status status--error" role="alert">{{ error() }}</p>
	  } @else {
		@if (movies.length === 0) {
		  <p class="status" aria-live="polite">No movies found.</p>
		} @else {
		  <ul class="movies" aria-label="Movie list">
			@for (m of movies; track m.id) {
			  <li
				class="movies__item"
				role="link"
				tabindex="0"
				(click)="openMovie(m)"
				(keydown.enter)="openMovie(m)"
				(keydown.space)="openMovie(m)"
			  >
				<div class="movies__title">
				  {{ m.title }}
				  @if (m.releaseYear) {
					<span class="movies__year">({{ m.releaseYear }})</span>
				  }
				</div>

				@if (m.genre) {
				  <div class="movies__meta">{{ m.genre }}</div>
				}

				@if (m.description) {
				  <p class="movies__desc">{{ m.description }}</p>
				}
			  </li>
			}
		  </ul>
		}
	  }
	</section>
  `,
  styleUrl: './movie-list.component.css',
})
export class MovieListComponent implements OnInit {
  movies: any[] = [];
  loading = signal<boolean>(true);
  error = signal<string | null>(null);

  constructor(
	private movieService: MovieService,
	private router: Router
  ) {}

  ngOnInit(): void {
	this.reload();
  }

  reload(): void {
	this.loading.set(true);
	this.error.set(null);

	this.movieService.getAllMovies().subscribe({
	  next: (movies) => {
		this.movies = movies ?? [];
		this.loading.set(false);
	  },
	  error: (err) => {
		const message =
		  (err && typeof err === 'object' && 'message' in err && (err as any).message) ||
		  'Failed to load movies.';
		this.error.set(String(message));
		this.loading.set(false);
	  }
	});
  }

  openMovie(movie: any): void {
	const id = movie?.id;
	if (typeof id !== 'number' && typeof id !== 'string') return;
	this.router.navigate(['/movies', id]);
  }
}

