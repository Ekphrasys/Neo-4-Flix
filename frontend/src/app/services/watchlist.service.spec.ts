import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';

import { WatchlistService } from './watchlist.service';

describe('WatchlistService', () => {
  let service: WatchlistService;
  let httpMock: HttpTestingController;
  const baseUrl = 'http://localhost:8082/api/watchlist';
  const movieId = '550e8400-e29b-41d4-a716-446655440000';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [WatchlistService],
    });
    service = TestBed.inject(WatchlistService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('requests current user watchlist', () => {
    const payload = [{ id: movieId, title: 'Inception' }];

    service.getMyWatchlist().subscribe((movies) => {
      expect(movies).toEqual(payload);
    });

    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('GET');
    req.flush(payload);
  });

  it('adds a movie', () => {
    service.add(movieId).subscribe();

    const req = httpMock.expectOne(`${baseUrl}/${movieId}`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toBeNull();
    req.flush(null);
  });

  it('removes a movie', () => {
    service.remove(movieId).subscribe();

    const req = httpMock.expectOne(`${baseUrl}/${movieId}`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('checks if movie exists in watchlist', () => {
    service.exists(movieId).subscribe((exists) => {
      expect(exists).toBeTrue();
    });

    const req = httpMock.expectOne(`${baseUrl}/${movieId}/exists`);
    expect(req.request.method).toBe('GET');
    req.flush(true);
  });
});

