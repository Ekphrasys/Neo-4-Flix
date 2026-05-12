import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormControl, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { debounceTime, distinctUntilChanged, map } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { MovieSearchParams, MovieService } from '../../services/movie.service';

@Component({
  selector: 'app-movie-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
	<section class="page">
	  <header class="page__header">
		<h1>Movies</h1>
		<form class="search" [formGroup]="filters" (submit)="$event.preventDefault()">
		  <input
			type="text"
			placeholder="Search by title..."
			class="search-input"
			formControlName="q"
			autocomplete="off"
		  >

		  <div class="search__row">
			<label class="search__label">
			  Genre
			  <select class="search__control" formControlName="genre">
				<option value="">All</option>
				@for (g of genres; track g) {
				  <option [value]="g">{{ g }}</option>
				}
			  </select>
			</label>

			<label class="search__label">
			  From
			  <input class="search__control" type="number" formControlName="releaseYearFrom" placeholder="e.g. 1990">
			</label>

			<label class="search__label">
			  To
			  <input class="search__control" type="number" formControlName="releaseYearTo" placeholder="e.g. 2024">
			</label>

			<button class="search__reset" type="button" (click)="resetFilters()">Reset</button>
		  </div>
		</form>
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

	private readonly destroyRef = inject(DestroyRef);

	readonly genres: string[] = [
	  'ACTION',
	  'HORROR',
	  'SCI_FI',
	  'ROMANCE',
	  'FANTASY',
	  'ANIMATION',
	  'DOCUMENTARY',
	];

	readonly filters = new FormGroup({
	  q: new FormControl<string>(''),
	  genre: new FormControl<string>(''),
	  releaseYearFrom: new FormControl<number | null>(null),
	  releaseYearTo: new FormControl<number | null>(null),
	});

  constructor(
	private movieService: MovieService,
	private router: Router
  ) {}

  ngOnInit(): void {
	this.filters.valueChanges
	  .pipe(
		debounceTime(300),
		map(() => this.filters.getRawValue()),
		distinctUntilChanged((a, b) => JSON.stringify(a) === JSON.stringify(b)),
		takeUntilDestroyed(this.destroyRef)
	  )
	  .subscribe(() => this.applyFilters());

	this.applyFilters();
  }


  reload(): void {
	this.resetFilters();
	this.applyFilters();
  }

  resetFilters(): void {
	this.filters.reset({
	  q: '',
	  genre: '',
	  releaseYearFrom: null,
	  releaseYearTo: null,
	});
  }

  private applyFilters(): void {
	const raw = this.filters.getRawValue();

	const q = raw.q?.trim() ?? '';
	const genre = raw.genre?.trim() ?? '';
	const releaseYearFrom = raw.releaseYearFrom ?? null;
	const releaseYearTo = raw.releaseYearTo ?? null;

	if (releaseYearFrom != null && releaseYearTo != null && releaseYearFrom > releaseYearTo) {
	  this.error.set('Invalid year range (From must be <= To).');
	  this.movies = [];
	  this.loading.set(false);
	  return;
	}

	const params: MovieSearchParams = {};
	if (q) params.q = q;
	if (genre) params.genre = genre;
	if (releaseYearFrom != null) params.releaseYearFrom = releaseYearFrom;
	if (releaseYearTo != null) params.releaseYearTo = releaseYearTo;

	const hasFilters = Object.keys(params).length > 0;

	this.loading.set(true);
	this.error.set(null);

	const request$ = hasFilters
	  ? this.movieService.searchMovies(params)
	  : this.movieService.getAllMovies();

	request$.subscribe({
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
	  },
	});
  }

  openMovie(movie: any): void {
	const id = movie?.id;
	if (typeof id !== 'number' && typeof id !== 'string') return;
	this.router.navigate(['/movies', id]);
  }
}

