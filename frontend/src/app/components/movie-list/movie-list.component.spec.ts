import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { MovieListComponent } from './movie-list.component';
import { MovieService } from '../../services/movie.service';

describe('MovieListComponent', () => {
  let component: MovieListComponent;
  let fixture: ComponentFixture<MovieListComponent>;
  let movieServiceSpy: jasmine.SpyObj<MovieService>;

  beforeEach(async () => {
    movieServiceSpy = jasmine.createSpyObj<MovieService>('MovieService', [
      'getAllMovies',
      'searchMovies',
    ]);
    movieServiceSpy.getAllMovies.and.returnValue(of([]));
    movieServiceSpy.searchMovies.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [MovieListComponent],
      providers: [provideRouter([]), { provide: MovieService, useValue: movieServiceSpy }],
    }).compileComponents();

    fixture = TestBed.createComponent(MovieListComponent);
    component = fixture.componentInstance;
  });

  it('creates the component', () => {
    expect(component).toBeTruthy();
  });

  it('loads movies and clears loading state', () => {
    const movies = [{ id: 1, title: 'Inception' }];
    movieServiceSpy.getAllMovies.and.returnValue(of(movies));

    component.reload();

    expect(component.movies).toEqual(movies);
    expect(component.loading()).toBeFalse();
    expect(component.error()).toBeNull();
  });

  it('sets error message when loading movies fails', () => {
    movieServiceSpy.getAllMovies.and.returnValue(
      throwError(() => new Error('Network unavailable'))
    );

    component.reload();

    expect(component.movies).toEqual([]);
    expect(component.loading()).toBeFalse();
    expect(component.error()).toBe('Network unavailable');
  });

  it('triggers search when filters change', (done) => {
    const payload = [{ id: 1, title: 'Inception' }];
    movieServiceSpy.searchMovies.and.returnValue(of(payload));

    component.ngOnInit();

    component.filters.controls.q.setValue('incep');

	setTimeout(() => {
	  expect(movieServiceSpy.searchMovies).toHaveBeenCalled();
	  done();
	}, 350);
  });
});
