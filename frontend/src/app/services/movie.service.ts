import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';


export interface MovieSearchParams {
  q?: string;
  title?: string;
  genre?: string;
  releaseYearFrom?: number;
  releaseYearTo?: number;
}

@Injectable({
  providedIn: 'root'
})
export class MovieService {
  private readonly baseUrl = 'http://localhost:8082/api/movies';

  constructor(private readonly http: HttpClient) {}

  getAllMovies(): Observable<any> {
	return this.http.get(this.baseUrl);
  }

  getMovieById(id: number): Observable<any> {
	return this.http.get(`${this.baseUrl}/${id}`);
  }

  searchMovies(params: MovieSearchParams): Observable<any> {
    let httpParams = new HttpParams();

    if (params.q) httpParams = httpParams.set('q', params.q);
    if (params.title) httpParams = httpParams.set('title', params.title);
    if (params.genre) httpParams = httpParams.set('genre', params.genre);
    if (params.releaseYearFrom != null) httpParams = httpParams.set('releaseYearFrom', String(params.releaseYearFrom));
    if (params.releaseYearTo != null) httpParams = httpParams.set('releaseYearTo', String(params.releaseYearTo));

    return this.http.get(this.baseUrl, { params: httpParams });
  }
}

