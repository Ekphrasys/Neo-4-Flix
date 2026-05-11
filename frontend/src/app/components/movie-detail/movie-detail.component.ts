import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { MovieService } from '../../services/movie.service';

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

  constructor(
    private readonly movieService: MovieService,
    private readonly route: ActivatedRoute,
    private readonly router: Router
  ) {}

  private loadMovie(id: number): void {
    this.loading.set(true);
    this.error.set(null);
    this.movie.set(null);

    this.movieService.getMovieById(id).subscribe({
      next: (movie) => {
        if (movie == null) {
          this.error.set('Movie not found.');
          this.movie.set(null);
        } else {
          this.movie.set(movie);
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
}
