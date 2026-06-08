import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class RecommendationService {
  private readonly baseUrl = '/api/recommendations';

  constructor(private readonly http: HttpClient) { }

  getRecommendations(params: { genre?: string; releaseYearFrom?: number; releaseYearTo?: number }): Observable<any> {
    let httpParams = new HttpParams();
    if (params.genre) httpParams = httpParams.set('genre', params.genre);
    if (params.releaseYearFrom != null) httpParams = httpParams.set('releaseYearFrom', String(params.releaseYearFrom));
    if (params.releaseYearTo != null) httpParams = httpParams.set('releaseYearTo', String(params.releaseYearTo));

    return this.http.get(this.baseUrl, { params: httpParams });
  }

  shareRecommendation(movieId: string, recipientId: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/share`, { movieId, recipientId });
  }

  getSharedRecommendations(): Observable<any> {
    return this.http.get(`${this.baseUrl}/shared`);
  }
}
