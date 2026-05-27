import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class WatchlistService {
  private readonly baseUrl = 'http://localhost:8082/api/watchlist';

  constructor(private readonly http: HttpClient) {}

  getMyWatchlist(): Observable<any[]> {
    return this.http.get<any[]>(this.baseUrl);
  }

  add(movieId: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${movieId}`, null);
  }

  remove(movieId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${movieId}`);
  }

  exists(movieId: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.baseUrl}/${movieId}/exists`);
  }
}

