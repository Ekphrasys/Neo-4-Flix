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
    const payload = [{ id: 1, title: 'Interstellar' }];

    service.getAllMovies().subscribe((movies) => {
      expect(movies).toEqual(payload);
    });

    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('GET');
    req.flush(payload);
  });

  it('requests one movie by id', () => {
    const payload = { id: 7, title: 'The Matrix' };

    service.getMovieById(7).subscribe((movie) => {
      expect(movie).toEqual(payload);
    });

    const req = httpMock.expectOne(`${baseUrl}/7`);
    expect(req.request.method).toBe('GET');
    req.flush(payload);
  });
});
