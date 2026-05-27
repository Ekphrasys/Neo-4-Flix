import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';

import { MovieService } from './movie.service';

describe('MovieService', () => {
  let service: MovieService;
  let httpMock: HttpTestingController;
  const baseUrl = 'http://localhost:8082/api/movies';
  const movieId1 = '550e8400-e29b-41d4-a716-446655440000';
  const movieId2 = '550e8400-e29b-41d4-a716-446655440001';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [MovieService],
    });
    service = TestBed.inject(MovieService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('requests all movies', () => {
    const payload = [{ id: movieId1, title: 'Interstellar' }];

    service.getAllMovies().subscribe((movies) => {
      expect(movies).toEqual(payload);
    });

    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('GET');
    req.flush(payload);
  });

  it('requests one movie by id', () => {
    const payload = { id: movieId2, title: 'The Matrix' };

    service.getMovieById(movieId2).subscribe((movie) => {
      expect(movie).toEqual(payload);
    });

    const req = httpMock.expectOne(`${baseUrl}/${movieId2}`);
    expect(req.request.method).toBe('GET');
    req.flush(payload);
  });

  it('searches movies with query params', () => {
    const payload = [{ id: movieId2, title: 'The Matrix' }];

    service
      .searchMovies({
        q: 'matrix',
        genre: 'SCI_FI',
        releaseYearFrom: 1990,
        releaseYearTo: 2000,
      })
      .subscribe((movies) => {
        expect(movies).toEqual(payload);
      });

    const req = httpMock.expectOne((r) => {
      return (
        r.url === baseUrl &&
        r.params.get('q') === 'matrix' &&
        r.params.get('genre') === 'SCI_FI' &&
        r.params.get('releaseYearFrom') === '1990' &&
        r.params.get('releaseYearTo') === '2000'
      );
    });
    expect(req.request.method).toBe('GET');
    req.flush(payload);
  });
});
