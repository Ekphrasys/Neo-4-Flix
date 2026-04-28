import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';


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
}

