import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface RatingStats {
  averageRating: number;
  totalRatings: number;
}

@Injectable({
  providedIn: 'root',
})
export class RatingService {
  private readonly baseUrl = '/api/ratings';

  constructor(private readonly http: HttpClient) {}

  getUserRating(movieId: string): Observable<{ rating: number | null }> {
    return this.http.get<{ rating: number | null }>(`${this.baseUrl}/movie/${movieId}/user`);
  }

  saveRating(movieId: string, rating: number): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/movie/${movieId}/user`, { rating });
  }

  deleteRating(movieId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/movie/${movieId}/user`);
  }

  getAverageRating(movieId: string): Observable<RatingStats> {
    return this.http.get<RatingStats>(`${this.baseUrl}/movie/${movieId}/average`);
  }
}
