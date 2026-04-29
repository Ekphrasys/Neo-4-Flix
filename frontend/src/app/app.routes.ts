import { Routes } from '@angular/router';

import { MovieListComponent } from './components/movie-list/movie-list.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'movies' },
  { path: 'movies', component: MovieListComponent },
  { path: '**', redirectTo: 'movies' }
];
