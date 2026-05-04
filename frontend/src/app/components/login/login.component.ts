import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';

import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  template: `
  <section class="auth" aria-label="Sign in">
    <div class="auth__card">
      <header class="auth__header">
        <h1 class="auth__title">Sign In</h1>
      </header>

      <form class="form" [formGroup]="loginForm" (ngSubmit)="onSubmit()" novalidate>
        <div class="field">
          <label class="field__label" for="email">Email</label>
          <input
            class="field__control"
            type="email"
            formControlName="email"
            id="email"
            autocomplete="email"
            placeholder="Email..."
          />

          @if (loginForm.get('email')?.hasError('required') && loginForm.get('email')?.touched) {
            <p class="field__hint">Email is required.</p>
          }
          @if (loginForm.get('email')?.hasError('email') && loginForm.get('email')?.touched) {
            <p class="field__hint">Please enter a valid email.</p>
          }
        </div>

        <div class="field">
          <label class="field__label" for="password">Password</label>
          <input
            class="field__control"
            type="password"
            formControlName="password"
            id="password"
            autocomplete="current-password"
            placeholder="••••••••"
          />

          @if (loginForm.get('password')?.hasError('required') && loginForm.get('password')?.touched) {
            <p class="field__hint">Password is required.</p>
          }
          @if (loginForm.get('password')?.hasError('minlength') && loginForm.get('password')?.touched) {
            <p class="field__hint">Password must be at least 6 characters.</p>
          }
        </div>

        @if (error()) {
          <p class="alert" role="alert">{{ error() }}</p>
        }

        <button type="submit" class="btn btn--accent form__submit" [disabled]="loading()">
          @if (loading()) {
            <span class="spinner" aria-hidden="true"></span>
            Logging in...
          } @else {
            Sign In
          }
        </button>

        <p class="auth__footer">
          <a routerLink="/register">New to Neo-4-Flix ? Create an account</a>
        </p>
      </form>
    </div>
  </section>
  `,
  styleUrl: './login.component.css',
})
export class LoginComponent {
  loginForm: FormGroup;
  loading = signal<boolean>(false);
  error = signal<string | null>(null);

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    const { email, password } = this.loginForm.value;

    this.authService.login({ email, password }).subscribe({
      next: (response) => {
        if (response && response.token) {
          this.authService.setToken(response.token);
          this.router.navigate(['/']); // Redirect to home/movies
        } else {
          this.error.set('Login failed. No token received.');
          this.loading.set(false);
        }
      },
      error: (err) => {
        console.error('Login error', err);
        const errorMsg =
          err?.error?.error ||
          err?.message ||
          'Failed to login. Please check your credentials.';
        this.error.set(errorMsg);
        this.loading.set(false);
      },
    });
  }
}
