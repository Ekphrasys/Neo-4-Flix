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
    const payload = [{ id: 1, title: 'Inception' }];

    service.getMyWatchlist().subscribe((movies) => {
      expect(movies).toEqual(payload);
    });

    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('GET');
    req.flush(payload);
  });

  it('adds a movie', () => {
    service.add(7).subscribe();

    const req = httpMock.expectOne(`${baseUrl}/7`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toBeNull();
    req.flush(null);
  });

  it('removes a movie', () => {
    service.remove(7).subscribe();

    const req = httpMock.expectOne(`${baseUrl}/7`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('checks if movie exists in watchlist', () => {
    service.exists(7).subscribe((exists) => {
      expect(exists).toBeTrue();
    });

    const req = httpMock.expectOne(`${baseUrl}/7/exists`);
    expect(req.request.method).toBe('GET');
    req.flush(true);
  });
});

