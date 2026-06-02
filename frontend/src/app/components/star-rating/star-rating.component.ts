import { CommonModule } from '@angular/common';
import { Component, Input, OnInit, signal } from '@angular/core';
import { RatingService } from '../../services/rating.service';

@Component({
  selector: 'app-star-rating',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="star-rating" [class.star-rating--readonly]="readonly">
      <!-- Average Rating Display -->
      <div class="star-rating__average">
        <span class="star-rating__avg-stars" [title]="'Average rating: ' + (averageRating() | number:'1.1-1') + '/5'">
          @for (star of [1, 2, 3, 4, 5]; track star) {
            <span class="star-rating__avg-star" 
                  [class.star-rating__avg-star--filled]="star <= averageRating()"
                  [class.star-rating__avg-star--half]="star - 0.5 <= averageRating() && star > averageRating()"
            >★</span>
          }
        </span>
        <span class="star-rating__avg-val">
          @if (totalRatings() > 0) {
            {{ averageRating() | number:'1.1-1' }}/5
          } @else {
            N/A
          }
        </span>
        <span class="star-rating__avg-count">
          ({{ totalRatings() }} {{ totalRatings() === 1 ? 'vote' : 'votes' }})
        </span>
      </div>

      <!-- Interactive User Vote Row -->
      @if (!readonly) {
        @if (inWatchlist) {
          <div class="star-rating__interactive">
            <span class="star-rating__user-label">Your rating:</span>
            <div class="star-rating__stars" (mouseleave)="clearHover()">
              @for (star of [1, 2, 3, 4, 5]; track star) {
                <button 
                  type="button" 
                  class="star-rating__star-btn"
                  [class.star-rating__star-btn--active]="star <= (hoveredRating() || userRating() || 0)"
                  (mouseenter)="hoverRating(star)"
                  (click)="rate(star)"
                  [disabled]="busy()"
                  [attr.aria-label]="'Rate ' + star + ' stars'"
                >
                  ★
                </button>
              }
            </div>
            
            @if (userRating() !== null) {
              <button 
                type="button" 
                class="star-rating__reset-btn"
                (click)="deleteRating()"
                [disabled]="busy()"
                title="Delete my rating"
              >
                ✕
              </button>
            }
          </div>
        } @else {
          <div class="star-rating__info-msg">
            💡 <em>Ajoutez ce film à votre watchlist pour le noter.</em>
          </div>
        }
      }
    </div>
  `,
  styleUrl: './star-rating.component.css'
})
export class StarRatingComponent implements OnInit {
  @Input({ required: true }) movieId!: string;
  @Input() readonly: boolean = false;
  @Input() inWatchlist: boolean = true;

  averageRating = signal<number>(0);
  totalRatings = signal<number>(0);
  userRating = signal<number | null>(null);
  hoveredRating = signal<number | null>(null);
  busy = signal<boolean>(false);

  constructor(private readonly ratingService: RatingService) {}

  ngOnInit(): void {
    this.loadStats();
    if (!this.readonly) {
      this.loadUserRating();
    }
  }

  loadStats(): void {
    this.ratingService.getAverageRating(this.movieId).subscribe({
      next: (stats) => {
        this.averageRating.set(stats?.averageRating ?? 0);
        this.totalRatings.set(stats?.totalRatings ?? 0);
      },
      error: () => {
        // Fallback or ignore
      }
    });
  }

  loadUserRating(): void {
    this.ratingService.getUserRating(this.movieId).subscribe({
      next: (res) => {
        this.userRating.set(res?.rating ?? null);
      },
      error: () => {
        // Fallback or ignore
      }
    });
  }

  hoverRating(star: number): void {
    if (this.busy()) return;
    this.hoveredRating.set(star);
  }

  clearHover(): void {
    this.hoveredRating.set(null);
  }

  rate(rating: number): void {
    if (this.busy()) return;
    this.busy.set(true);
    this.ratingService.saveRating(this.movieId, rating).subscribe({
      next: () => {
        this.userRating.set(rating);
        this.busy.set(false);
        this.loadStats();
      },
      error: () => {
        this.busy.set(false);
      }
    });
  }

  deleteRating(): void {
    if (this.busy()) return;
    this.busy.set(true);
    this.ratingService.deleteRating(this.movieId).subscribe({
      next: () => {
        this.userRating.set(null);
        this.busy.set(false);
        this.loadStats();
      },
      error: () => {
        this.busy.set(false);
      }
    });
  }
}
