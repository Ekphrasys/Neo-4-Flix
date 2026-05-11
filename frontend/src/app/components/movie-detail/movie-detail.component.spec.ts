import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { MovieDetailComponent } from './movie-detail.component';
import { MovieService } from '../../services/movie.service';
import { RatingService } from '../../services/rating.service';

describe('MovieDetailComponent', () => {
  let fixture: ComponentFixture<MovieDetailComponent>;
  let component: MovieDetailComponent;
  let movieServiceSpy: jasmine.SpyObj<MovieService>;
  let ratingServiceSpy: jasmine.SpyObj<RatingService>;

  beforeEach(async () => {
	movieServiceSpy = jasmine.createSpyObj<MovieService>('MovieService', ['getMovieById']);
	ratingServiceSpy = jasmine.createSpyObj<RatingService>('RatingService', ['getRatingsByMovieId']);

	movieServiceSpy.getMovieById.and.returnValue(
	  of({ id: 1, title: 'Inception', description: 'Dreams', releaseYear: 2010, genre: 'SCI_FI' })
	);
	ratingServiceSpy.getRatingsByMovieId.and.returnValue(of([{ id: 1, rating: 5 }, { id: 2, rating: 3 }]));

	await TestBed.configureTestingModule({
	  imports: [MovieDetailComponent],
	  providers: [
		provideRouter([]),
		{ provide: MovieService, useValue: movieServiceSpy },
		{ provide: RatingService, useValue: ratingServiceSpy },
		{
		  provide: ActivatedRoute,
		  useValue: {
			paramMap: of(convertToParamMap({ id: '1' })),
		  },
		},
	  ],
	}).compileComponents();

	fixture = TestBed.createComponent(MovieDetailComponent);
	component = fixture.componentInstance;
	fixture.detectChanges();
  });

  it('creates the component', () => {
	expect(component).toBeTruthy();
  });

  it('loads the movie and ratings', () => {
	expect(movieServiceSpy.getMovieById).toHaveBeenCalledWith(1);
	expect(ratingServiceSpy.getRatingsByMovieId).toHaveBeenCalledWith(1);

	expect(component.error()).toBeNull();
	expect(component.loading()).toBeFalse();
	expect(component.movie()?.title).toBe('Inception');
	expect(component.ratingCount()).toBe(2);
	expect(component.avgRating()).toBe(4);
  });

  it('sets an error when movie loading fails', async () => {
	movieServiceSpy.getMovieById.and.returnValue(throwError(() => ({ status: 404 })));

	await TestBed.resetTestingModule();
	await TestBed.configureTestingModule({
	  imports: [MovieDetailComponent],
	  providers: [
		provideRouter([]),
		{ provide: MovieService, useValue: movieServiceSpy },
		{ provide: RatingService, useValue: ratingServiceSpy },
		{
		  provide: ActivatedRoute,
		  useValue: {
			paramMap: of(convertToParamMap({ id: '1' })),
		  },
		},
	  ],
	}).compileComponents();

	const f = TestBed.createComponent(MovieDetailComponent);
	const c = f.componentInstance;
	f.detectChanges();

	expect(c.error()).toBe('Movie not found.');
	expect(c.movie()).toBeNull();
  });

  it('sets an error when id is invalid', async () => {
	await TestBed.resetTestingModule();
	await TestBed.configureTestingModule({
	  imports: [MovieDetailComponent],
	  providers: [
		provideRouter([]),
		{ provide: MovieService, useValue: movieServiceSpy },
		{ provide: RatingService, useValue: ratingServiceSpy },
		{
		  provide: ActivatedRoute,
		  useValue: {
			paramMap: of(convertToParamMap({ id: 'nope' })),
		  },
		},
	  ],
	}).compileComponents();

	const f = TestBed.createComponent(MovieDetailComponent);
	const c = f.componentInstance;
	f.detectChanges();

	expect(c.error()).toBe('Invalid movie id.');
	expect(c.loading()).toBeFalse();
  });
});
