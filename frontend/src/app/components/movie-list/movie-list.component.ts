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

		<button type="button" class="btn btn--accent" (click)="reload()" [disabled]="loading()">
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
		padding: 0; /* le container global gère déjà le padding */
	  }

	  .page__header {
		display: flex;
		align-items: end;
		justify-content: space-between;
		gap: 1rem;
		margin-bottom: 1.1rem;
		padding: 1.15rem 1.1rem;
		border: 1px solid var(--n4f-border);
		border-radius: var(--n4f-radius);
		background:
			radial-gradient(700px 180px at 20% 0%, rgba(229, 9, 20, 0.25), transparent 60%),
			linear-gradient(180deg, rgba(255, 255, 255, 0.05), rgba(255, 255, 255, 0.02));
	  }

	  h1 {
		margin: 0;
		font-size: 1.65rem;
		letter-spacing: -0.02em;
		line-height: 1.1;
	  }

	  .status {
		margin: 1rem 0;
		color: var(--n4f-text-muted);
	  }

	  .status--error {
		color: color-mix(in srgb, var(--n4f-accent) 85%, white);
	  }

	  .movies {
		list-style: none;
		padding: 0;
		margin: 0;
		display: grid;
		gap: 0.9rem;
		grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
	  }

	  .movies__item {
		position: relative;
		overflow: hidden;
		border: 1px solid var(--n4f-border);
		border-radius: var(--n4f-radius);
		padding: 0.95rem 0.95rem 0.9rem;
		background:
			linear-gradient(180deg, rgba(255, 255, 255, 0.06), rgba(255, 255, 255, 0.02));
		box-shadow: 0 1px 0 rgba(255, 255, 255, 0.04) inset;
		transition: transform 160ms ease, border-color 160ms ease, box-shadow 160ms ease;
	  }

	  /* Petite barre d'accent en haut (effet “row highlight”) */
	  .movies__item::before {
		content: '';
		position: absolute;
		left: 0;
		top: 0;
		height: 3px;
		width: 100%;
		background: linear-gradient(90deg, var(--n4f-accent), rgba(229, 9, 20, 0));
		opacity: 0.55;
	  }

	  @media (hover: hover) {
		.movies__item:hover {
		  transform: translateY(-4px) scale(1.01);
		  border-color: rgba(255, 255, 255, 0.18);
		  box-shadow: var(--n4f-shadow);
		}
	  }

	  .movies__title {
		font-weight: 750;
		margin-bottom: 0.25rem;
	  }

	  .movies__year {
		font-weight: 550;
		color: var(--n4f-text-faint);
		margin-left: 0.35rem;
	  }

	  .movies__meta {
		color: var(--n4f-text-muted);
		font-size: 0.92rem;
		margin-bottom: 0.55rem;
	  }

	  .movies__desc {
		margin: 0;
		color: var(--n4f-text-muted);
		display: -webkit-box;
		-webkit-line-clamp: 3;
		-webkit-box-orient: vertical;
		overflow: hidden;
	  }

	  @media (max-width: 560px) {
		.page__header {
		  flex-direction: column;
		  align-items: stretch;
		}
		.page__header .btn {
		  width: 100%;
		  justify-content: center;
		}
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

