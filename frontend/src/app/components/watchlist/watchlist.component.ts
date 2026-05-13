import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';

import { WatchlistService } from '../../services/watchlist.service';

@Component({
  selector: 'app-watchlist',
  standalone: true,
  imports: [CommonModule],
  template: `
	<section class="page">
	  <header class="page__header">
		<h1>Your Watchlist</h1>
		<button type="button" class="btn" (click)="backToMovies()">Back to movies</button>
	  </header>

	  @if (loading()) {
		<p class="status" aria-live="polite">Loading…</p>
	  } @else if (error()) {
		<p class="status status--error" role="alert">{{ error() }}</p>
	  } @else {
		@if (movies().length === 0) {
		  <p class="status" aria-live="polite">Your watchlist is empty.</p>
		} @else {
		  <ul class="movies" aria-label="Movie list">
			@for (m of movies(); track m.id) {
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

				<button
				  type="button"
				  class="btn btn--accent watchlist-btn"
				  (click)="remove(m, $event)"
				  [disabled]="removingId() === m.id"
				>
				  @if (removingId() === m.id) { Removing… } @else { Remove }
				</button>
			  </li>
			}
		  </ul>
		}
	  }
	</section>
  `,
  styleUrl: './watchlist.component.css',
})
export class WatchlistComponent implements OnInit {
  movies = signal<any[]>([]);
  loading = signal<boolean>(true);
  error = signal<string | null>(null);
  removingId = signal<number | null>(null);

  constructor(
	private readonly watchlistService: WatchlistService,
	private readonly router: Router
  ) {}

  ngOnInit(): void {
	this.load();
  }

  private load(): void {
	this.loading.set(true);
	this.error.set(null);
	this.watchlistService.getMyWatchlist().subscribe({
	  next: (movies) => {
		this.movies.set(movies ?? []);
		this.loading.set(false);
	  },
	  error: () => {
		this.error.set('Failed to load watchlist.');
		this.loading.set(false);
	  },
	});
  }

  openMovie(movie: any): void {
	const id = movie?.id;
	if (typeof id !== 'number' && typeof id !== 'string') return;
	this.router.navigate(['/movies', id]);
  }

  remove(movie: any, event: Event): void {
	event.stopPropagation();
	const id = movie?.id;
	if (typeof id !== 'number') return;

	this.removingId.set(id);
	this.watchlistService.remove(id).subscribe({
	  next: () => {
		this.removingId.set(null);
		this.load();
	  },
	  error: () => {
		this.removingId.set(null);
		this.error.set('Failed to remove movie from watchlist.');
	  },
	});
  }

  backToMovies(): void {
	this.router.navigate(['/movies']);
  }
}


