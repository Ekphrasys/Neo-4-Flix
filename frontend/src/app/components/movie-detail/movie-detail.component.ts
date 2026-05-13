import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { MovieService } from '../../services/movie.service';
import { WatchlistService } from '../../services/watchlist.service';

@Component({
  selector: 'app-movie-detail',
  standalone: true,
  imports: [CommonModule],
  template: `
  <section class="page">
    <button type="button" class="back-btn" (click)="backToList()">Back</button>
    @if (loading()) {
      <p class="status" aria-live="polite">Loading…</p>
    } @else if (error()) {
      <p class="status status--error" role="alert">{{ error() }}</p>
    } @else if (movie(); as m) {
      <div class="movie_details">
      <header class="page__header">
        <h1>{{ m.title }}</h1>

        <button
          type="button"
          class="btn btn--accent watchlist-btn"
          (click)="toggleWatchlist(m.id)"
          [disabled]="watchlistBusy()"
        >
          @if (watchlistBusy()) {
            Working…
          } @else if (inWatchlist()) {
            Remove from watchlist
          } @else {
            Add to watchlist
          }
        </button>

        @if (watchlistError()) {
          <p class="status status--error" role="alert">{{ watchlistError() }}</p>
        }
      </header>

      <div class="movie-detail">
        @if (m.releaseYear) {
          <p><strong>Release Year:</strong> {{ m.releaseYear }}</p>
        }

        @if (m.genre) {
          <p><strong>Genre:</strong> {{ m.genre }}</p>
        }

        @if (m.description) {
          <p><strong>Description:</strong> {{ m.description }}</p>
        }
      </div>
      </div>
    }
  </section>
  `,
  styleUrl: './movie-detail.component.css'
})

export class MovieDetailComponent implements OnInit {
  movie = signal<any | null>(null);
  loading = signal<boolean>(true);
  error = signal<string | null>(null);

  inWatchlist = signal<boolean>(false);
  watchlistBusy = signal<boolean>(false);
  watchlistError = signal<string | null>(null);

  constructor(
    private readonly movieService: MovieService,
    private readonly watchlistService: WatchlistService,
    private readonly route: ActivatedRoute,
    private readonly router: Router
  ) {}

  private loadMovie(id: number): void {
    this.loading.set(true);
    this.error.set(null);
    this.movie.set(null);
    this.inWatchlist.set(false);
    this.watchlistError.set(null);

    this.movieService.getMovieById(id).subscribe({
      next: (movie) => {
        if (movie == null) {
          this.error.set('Movie not found.');
          this.movie.set(null);
        } else {
          this.movie.set(movie);

          this.watchlistService.exists(id).subscribe({
            next: (exists) => this.inWatchlist.set(!!exists),
            error: () => {
              // on ne bloque pas l'écran si l'endpoint existe échoue
              this.inWatchlist.set(false);
            },
          });
        }
        this.loading.set(false);
      },
      error: (err) => {
        if (err && typeof err === 'object' && 'status' in err && (err as any).status === 404) {
          this.error.set('Movie not found.');
        } else {
          this.error.set('Failed to load movie.');
        }
        this.loading.set(false);
      },
    });
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const rawId = params.get('id');
      const id = rawId == null ? NaN : Number(rawId);

      if (!Number.isFinite(id)) {
        this.error.set('Invalid movie id.');
        this.loading.set(false);
        return;
      }

      this.loadMovie(id);
    });
  }

  backToList(): void {
    this.router.navigate(['/movies']);
  }

  toggleWatchlist(movieId: number): void {
    if (!Number.isFinite(movieId)) return;
    this.watchlistBusy.set(true);
    this.watchlistError.set(null);

    const req$ = this.inWatchlist()
      ? this.watchlistService.remove(movieId)
      : this.watchlistService.add(movieId);

    req$.subscribe({
      next: () => {
        this.inWatchlist.set(!this.inWatchlist());
        this.watchlistBusy.set(false);
      },
      error: () => {
        this.watchlistError.set('Failed to update watchlist.');
        this.watchlistBusy.set(false);
      },
    });
  }
}
