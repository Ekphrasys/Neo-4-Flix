import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';

import { MovieService } from '../../services/movie.service';

@Component({
  selector: 'app-movie-list',
  standalone: true,
  imports: [CommonModule],
  template: `
	<section class="page">
	  <header class="page__header">
		<h1>Movies</h1>

		<button type="button" class="btn" (click)="reload()" [disabled]="loading()">
		  Refresh
		</button>
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
			  <li class="movies__item">
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
  styles: [
	`
	  .page {
		max-width: 900px;
		margin: 0 auto;
		padding: 1.25rem;
		font-family: system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial, sans-serif;
	  }

	  .page__header {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 1rem;
		margin-bottom: 1rem;
	  }

	  h1 {
		margin: 0;
		font-size: 1.75rem;
	  }

	  .btn {
		padding: 0.5rem 0.75rem;
		border: 1px solid #d0d5dd;
		background: #fff;
		border-radius: 0.5rem;
		cursor: pointer;
	  }

	  .btn:disabled {
		opacity: 0.65;
		cursor: not-allowed;
	  }

	  .status {
		margin: 1rem 0;
		color: #344054;
	  }

	  .status--error {
		color: #b42318;
	  }

	  .movies {
		list-style: none;
		padding: 0;
		margin: 0;
		display: grid;
		gap: 0.75rem;
	  }

	  .movies__item {
		border: 1px solid #eaecf0;
		border-radius: 0.75rem;
		padding: 0.9rem;
		background: #fff;
	  }

	  .movies__title {
		font-weight: 600;
		margin-bottom: 0.25rem;
	  }

	  .movies__year {
		font-weight: 400;
		color: #667085;
		margin-left: 0.25rem;
	  }

	  .movies__meta {
		color: #667085;
		font-size: 0.9rem;
		margin-bottom: 0.5rem;
	  }

	  .movies__desc {
		margin: 0;
		color: #344054;
	  }
	`
  ]
})
export class MovieListComponent implements OnInit {
  movies: any[] = [];
  loading = signal<boolean>(true);
  error = signal<string | null>(null);

  constructor(private movieService: MovieService) {}

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
}

