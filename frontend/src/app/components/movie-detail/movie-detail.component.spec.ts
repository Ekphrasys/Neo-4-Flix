import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';

import { MovieDetailComponent } from './movie-detail.component';
import { MovieService } from '../../services/movie.service';
import { WatchlistService } from '../../services/watchlist.service';

describe('MovieDetailComponent', () => {
  let component: MovieDetailComponent;
  let fixture: ComponentFixture<MovieDetailComponent>;
  let movieServiceSpy: jasmine.SpyObj<MovieService>;
  let watchlistServiceSpy: jasmine.SpyObj<WatchlistService>;
  let activatedRouteSpy: any;
  const movieId = '550e8400-e29b-41d4-a716-446655440000';

  beforeEach(async () => {
    movieServiceSpy = jasmine.createSpyObj<MovieService>('MovieService', [
      'getMovieById',
    ]);
    watchlistServiceSpy = jasmine.createSpyObj<WatchlistService>('WatchlistService', [
      'add',
      'remove',
      'exists',
    ]);

    activatedRouteSpy = {
      paramMap: of({ get: (key: string) => movieId }),
    };

    await TestBed.configureTestingModule({
      imports: [MovieDetailComponent],
      providers: [
        { provide: MovieService, useValue: movieServiceSpy },
        { provide: WatchlistService, useValue: watchlistServiceSpy },
        { provide: ActivatedRoute, useValue: activatedRouteSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(MovieDetailComponent);
    component = fixture.componentInstance;
  });

  it('creates the component', () => {
    expect(component).toBeTruthy();
  });

  it('loads movie details when id is provided', () => {
    const movieData = { id: movieId, title: 'Inception', releaseYear: 2010 };
    movieServiceSpy.getMovieById.and.returnValue(of(movieData));
    watchlistServiceSpy.exists.and.returnValue(of(false));

    component.ngOnInit();

    expect(movieServiceSpy.getMovieById).toHaveBeenCalledWith(movieId);
    expect(component.movie()).toEqual(movieData);
    expect(component.loading()).toBeFalse();
  });

  it('sets error when movie is not found', () => {
    movieServiceSpy.getMovieById.and.returnValue(of(null));

    component.ngOnInit();

    expect(component.error()).toBe('Movie not found.');
    expect(component.loading()).toBeFalse();
  });

  it('adds movie to watchlist', () => {
    const movieData = { id: movieId, title: 'Inception' };
    movieServiceSpy.getMovieById.and.returnValue(of(movieData));
    watchlistServiceSpy.exists.and.returnValue(of(false));
    watchlistServiceSpy.add.and.returnValue(of(void 0));

    component.ngOnInit();
    component.toggleWatchlist(movieId);

    expect(watchlistServiceSpy.add).toHaveBeenCalledWith(movieId);
    expect(component.inWatchlist()).toBeTrue();
  });

  it('removes movie from watchlist', () => {
    const movieData = { id: movieId, title: 'Inception' };
    movieServiceSpy.getMovieById.and.returnValue(of(movieData));
    watchlistServiceSpy.exists.and.returnValue(of(true));
    watchlistServiceSpy.remove.and.returnValue(of(void 0));

    component.ngOnInit();
    component.inWatchlist.set(true);
    component.toggleWatchlist(movieId);

    expect(watchlistServiceSpy.remove).toHaveBeenCalledWith(movieId);
    expect(component.inWatchlist()).toBeFalse();
  });
});

