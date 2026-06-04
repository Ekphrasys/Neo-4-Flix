import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormControl, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { debounceTime, distinctUntilChanged, map } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { RecommendationService } from '../../services/recommendation.service';
import { WatchlistService } from '../../services/watchlist.service';
import { AuthService } from '../../services/auth.service';
import { StarRatingComponent } from '../star-rating/star-rating.component';

@Component({
  selector: 'app-recommendations',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, StarRatingComponent],
  template: `
    <section class="page">
      <header class="page__header">
        <h1>Movie Recommendations</h1>
        <div class="tabs" role="tablist">
          <button 
            type="button" 
            class="tab-btn" 
            [class.tab-btn--active]="activeTab() === 'recommendations'"
            (click)="setTab('recommendations')"
            role="tab"
          >
            Recommended For You
          </button>
          <button 
            type="button" 
            class="tab-btn" 
            [class.tab-btn--active]="activeTab() === 'shared'"
            (click)="setTab('shared')"
            role="tab"
          >
            Shared By Friends
          </button>
        </div>
      </header>

      @if (activeTab() === 'recommendations') {
        <form class="search" [formGroup]="filters" (submit)="$event.preventDefault()">
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
              From Year
              <input class="search__control" type="number" formControlName="releaseYearFrom" placeholder="e.g. 1990">
            </label>

            <label class="search__label">
              To Year
              <input class="search__control" type="number" formControlName="releaseYearTo" placeholder="e.g. 2026">
            </label>

            <button class="search__reset" type="button" (click)="resetFilters()">Reset</button>
          </div>
        </form>

        @if (loadingRecs()) {
          <p class="status">Curating your recommendations...</p>
        } @else if (errorRecs()) {
          <p class="status status--error">{{ errorRecs() }}</p>
        } @else if (recommendations().length === 0) {
          <div class="empty-state">
            <p>No recommendations found matching your preferences.</p>
            <p class="empty-state__hint">Try rating more movies to train the graph recommendation engine!</p>
          </div>
        } @else {
          <ul class="movies" aria-label="Recommendation list">
            @for (m of recommendations(); track m.id) {
              <li 
                class="movies__item" 
                (click)="openMovie(m)"
                tabindex="0"
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

                <app-star-rating [movieId]="m.id" [readonly]="true"></app-star-rating>

                <div class="actions">
                  <button
                    type="button"
                    class="btn btn--accent watchlist-btn"
                    (click)="toggleWatchlist(m, $event)"
                    [disabled]="watchlistWorkingId() === m.id"
                  >
                    @if (watchlistWorkingId() === m.id) {
                      Working…
                    } @else if (watchlistIds().has(m.id)) {
                      Remove
                    } @else {
                      Add Watchlist
                    }
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

      @if (activeTab() === 'shared') {
        @if (loadingShared()) {
          <p class="status">Loading shared recommendations...</p>
        } @else if (errorShared()) {
          <p class="status status--error">{{ errorShared() }}</p>
        } @else if (sharedRecommendations().length === 0) {
          <div class="empty-state">
            <p>Your friends haven't shared any movies with you yet.</p>
          </div>
        } @else {
          <ul class="movies" aria-label="Shared list">
            @for (s of sharedRecommendations(); track s.movie.id) {
              <li 
                class="movies__item" 
                (click)="openMovie(s.movie)"
                tabindex="0"
              >
                <div class="share-info">
                  Shared by <span class="share-info__sender">&#64;{{ s.sharedByUsername }}</span>
                </div>

                <div class="movies__title">
                  {{ s.movie.title }}
                  @if (s.movie.releaseYear) {
                    <span class="movies__year">({{ s.movie.releaseYear }})</span>
                  }
                </div>

                @if (s.movie.genre) {
                  <div class="movies__meta">{{ s.movie.genre }}</div>
                }

                @if (s.movie.description) {
                  <p class="movies__desc">{{ s.movie.description }}</p>
                }

                <app-star-rating [movieId]="s.movie.id" [readonly]="true"></app-star-rating>

                <div class="actions">
                  <button
                    type="button"
                    class="btn btn--accent watchlist-btn"
                    (click)="toggleWatchlist(s.movie, $event)"
                    [disabled]="watchlistWorkingId() === s.movie.id"
                  >
                    @if (watchlistWorkingId() === s.movie.id) {
                      Working…
                    } @else if (watchlistIds().has(s.movie.id)) {
                      Remove
                    } @else {
                      Add Watchlist
                    }
                  </button>
                </div>
              </li>
            }
          </ul>
        }
      }
    </section>
  `,
  styleUrl: './recommendations.component.css'
})
export class RecommendationsComponent implements OnInit {
  recommendations = signal<any[]>([]);
  sharedRecommendations = signal<any[]>([]);
  friends = signal<any[]>([]);

  loadingRecs = signal<boolean>(true);
  loadingShared = signal<boolean>(false);
  errorRecs = signal<string | null>(null);
  errorShared = signal<string | null>(null);

  watchlistIds = signal<Set<string>>(new Set<string>());
  watchlistWorkingId = signal<string | null>(null);

  activeTab = signal<'recommendations' | 'shared'>('recommendations');
  sharingMovieId = signal<string | null>(null);
  sharingWorkingId = signal<string | null>(null);

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
    genre: new FormControl<string>(''),
    releaseYearFrom: new FormControl<number | null>(null),
    releaseYearTo: new FormControl<number | null>(null),
  });

  private readonly destroyRef = inject(DestroyRef);

  constructor(
    private readonly recommendationService: RecommendationService,
    private readonly watchlistService: WatchlistService,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.loadWatchlistIds();
    this.loadFriends();

    this.filters.valueChanges
      .pipe(
        debounceTime(300),
        map(() => this.filters.getRawValue()),
        distinctUntilChanged((a, b) => JSON.stringify(a) === JSON.stringify(b)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => this.loadRecommendations());

    this.loadRecommendations();
  }

  setTab(tab: 'recommendations' | 'shared'): void {
    this.activeTab.set(tab);
    if (tab === 'recommendations') {
      this.loadRecommendations();
    } else {
      this.loadSharedRecommendations();
    }
  }

  resetFilters(): void {
    this.filters.reset({
      genre: '',
      releaseYearFrom: null,
      releaseYearTo: null,
    });
  }

  loadRecommendations(): void {
    const raw = this.filters.getRawValue();
    const params: any = {};
    if (raw.genre) params.genre = raw.genre;
    if (raw.releaseYearFrom != null) params.releaseYearFrom = raw.releaseYearFrom;
    if (raw.releaseYearTo != null) params.releaseYearTo = raw.releaseYearTo;

    this.loadingRecs.set(true);
    this.errorRecs.set(null);

    this.recommendationService.getRecommendations(params).subscribe({
      next: (movies) => {
        this.recommendations.set(movies ?? []);
        this.loadingRecs.set(false);
      },
      error: (err) => {
        console.error('Failed to load recommendations', err);
        this.errorRecs.set('Failed to load recommendations.');
        this.loadingRecs.set(false);
      }
    });
  }

  loadSharedRecommendations(): void {
    this.loadingShared.set(true);
    this.errorShared.set(null);

    this.recommendationService.getSharedRecommendations().subscribe({
      next: (shared) => {
        this.sharedRecommendations.set(shared ?? []);
        this.loadingShared.set(false);
      },
      error: (err) => {
        console.error('Failed to load shared recommendations', err);
        this.errorShared.set('Failed to load shared recommendations.');
        this.loadingShared.set(false);
      }
    });
  }

  loadWatchlistIds(): void {
    this.watchlistService.getMyWatchlist().subscribe({
      next: (movies) => {
        const ids = new Set<string>();
        for (const m of movies ?? []) {
          const id = (m as any)?.id;
          if (typeof id === 'string') ids.add(id);
        }
        this.watchlistIds.set(ids);
      },
      error: () => {}
    });
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

  openMovie(movie: any): void {
    const id = movie?.id;
    if (typeof id !== 'string') return;
    this.router.navigate(['/movies', id]);
  }

  toggleWatchlist(movie: any, event: Event): void {
    event.stopPropagation();
    const id = movie?.id;
    if (typeof id !== 'string') return;

    this.watchlistWorkingId.set(id);
    const isIn = this.watchlistIds().has(id);
    const req$ = isIn ? this.watchlistService.remove(id) : this.watchlistService.add(id);

    req$.subscribe({
      next: () => {
        const nextSet = new Set(this.watchlistIds());
        if (isIn) nextSet.delete(id);
        else nextSet.add(id);
        this.watchlistIds.set(nextSet);
        this.watchlistWorkingId.set(null);
      },
      error: () => {
        this.watchlistWorkingId.set(null);
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
