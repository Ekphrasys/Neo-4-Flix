import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { Router } from '@angular/router';

import { WatchlistService } from '../../services/watchlist.service';
import { RecommendationService } from '../../services/recommendation.service';
import { AuthService } from '../../services/auth.service';

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

		<div class="actions">
		  <button
			type="button"
			class="btn btn--accent watchlist-btn"
			(click)="remove(m, $event)"
			[disabled]="removingId() === m.id"
		  >
			@if (removingId() === m.id) { Removing… } @else { Remove }
		  </button>

		  <button
			type="button"
			class="btn btn-share"
			(click)="openShareDropdown(m, $event)"
		  >
			Share ↗
		  </button>
		</div>

		<!-- Dropdown selector inline for sharing with friends -->
		@if (sharingMovieId() === m.id) {
		  <div class="share-dropdown" (click)="$event.stopPropagation()">
			<h4>Share with a Friend:</h4>
			@if (friends().length === 0) {
			  <p class="share-dropdown__no-friends">You are not following any friends yet. Add friends first!</p>
			} @else {
			  <ul class="friends-list">
				@for (f of friends(); track f.id) {
				  <li>
					<button
					  type="button"
					  class="friend-btn"
					  (click)="confirmShare(m.id, f.id)"
					  [disabled]="sharingWorkingId() === f.id"
					>
					  @if (sharingWorkingId() === f.id) {
						Sharing...
					  } @else {
						Share with <strong>{{ f.username }}</strong>
					  }
					</button>
				  </li>
				}
			  </ul>
			}
			<button type="button" class="btn-cancel-share" (click)="sharingMovieId.set(null)">Close</button>
		  </div>
		}
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
  removingId = signal<null>(null);

  friends = signal<any[]>([]);
  sharingMovieId = signal<string | null>(null);
  sharingWorkingId = signal<string | null>(null);

  private readonly destroyRef = inject(DestroyRef);

  constructor(
	private readonly watchlistService: WatchlistService,
	private readonly recommendationService: RecommendationService,
	private readonly authService: AuthService,
	private readonly router: Router
  ) {}

  ngOnInit(): void {
	this.load();
	this.loadFriends();
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
	if (typeof id !== 'string') return;
	this.router.navigate(['/movies', id]);
  }

  remove(movie: any, event: Event): void {
	event.stopPropagation();
	const id = movie?.id;
	// if (typeof id !== 'number') return;

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

  loadFriends(): void {
	this.authService.getFollowing().subscribe({
	  next: (followedIds) => {
		const followedSet = new Set(followedIds);
		this.authService.searchUsers('').subscribe({
		  next: (users) => {
			const followedUsers = (users ?? []).filter(u => followedSet.has(u.id));
			this.friends.set(followedUsers);
		  },
		  error: (err) => {
			console.error('Error fetching system users', err);
		  }
		});
	  },
	  error: (err) => {
		console.error('Error loading followed user IDs', err);
	  }
	});
  }

  openShareDropdown(movie: any, event: Event): void {
	event.stopPropagation();
	if (this.sharingMovieId() === movie.id) {
	  this.sharingMovieId.set(null);
	} else {
	  this.sharingMovieId.set(movie.id);
	}
  }

  confirmShare(movieId: string, recipientId: string): void {
	this.sharingWorkingId.set(recipientId);
	this.recommendationService.shareRecommendation(movieId, recipientId).subscribe({
	  next: () => {
		alert('Movie recommendation shared successfully!');
		this.sharingMovieId.set(null);
		this.sharingWorkingId.set(null);
	  },
	  error: (err) => {
		console.error('Sharing failed', err);
		alert('Failed to share recommendation. Make sure you follow this friend.');
		this.sharingWorkingId.set(null);
	  }
	});
  }
}


