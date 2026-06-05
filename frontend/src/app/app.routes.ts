import { Routes } from '@angular/router';

import { AuthGuard } from './services/auth.guard';

import { MovieListComponent } from './components/movie-list/movie-list.component';
import { MovieDetailComponent } from './components/movie-detail/movie-detail.component';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { WatchlistComponent } from './components/watchlist/watchlist.component';
import { TwoFactorSetupComponent } from './components/two-factor-setup/two-factor-setup.component';
import { MovieCreateComponent } from './components/create-movie/create-movie.component';
import { FriendsComponent } from './components/friends/friends.component';
import { RecommendationsComponent } from './components/recommendations/recommendations.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'movies' },
  { path: 'movies/:id', component: MovieDetailComponent },
  { path: 'movies', component: MovieListComponent },
  { path: 'friends', component: FriendsComponent, canActivate: [AuthGuard] },
  { path: 'watchlist', component: WatchlistComponent, canActivate: [AuthGuard] },
  { path: 'recommendations', component: RecommendationsComponent, canActivate: [AuthGuard] },
  { path: '2fa-setup', component: TwoFactorSetupComponent, canActivate: [AuthGuard] },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'create', component: MovieCreateComponent, canActivate: [AuthGuard] },

  // { path: '**', redirectTo: 'movies' }
];
