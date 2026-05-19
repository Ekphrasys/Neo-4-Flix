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

      <!-- Step 1: Email & Password -->
      @if (!requires2FA()) {
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
      }

      <!-- Step 2: 2FA Code -->
      @if (requires2FA()) {
        <form class="form" [formGroup]="totpForm" (ngSubmit)="onVerify2FA()" novalidate>
          <p class="auth__subtitle">Enter the 6-digit code from your authenticator app</p>

          <div class="field">
            <label class="field__label" for="totp-code">Authentication Code</label>
            <input
              class="field__control field__control--otp"
              type="text"
              formControlName="code"
              id="totp-code"
              autocomplete="one-time-code"
              placeholder="000000"
              maxlength="6"
              inputmode="numeric"
            />
          </div>

          @if (error()) {
            <p class="alert" role="alert">{{ error() }}</p>
          }

          <button type="submit" class="btn btn--accent form__submit" [disabled]="loading()">
            @if (loading()) {
              <span class="spinner" aria-hidden="true"></span>
              Verifying...
            } @else {
              Verify
            }
          </button>

          <p class="auth__footer">
            <a href="#" (click)="back($event)">← Back to login</a>
          </p>
        </form>
      }
    </div>
  </section>
  `,
  styleUrl: './login.component.css',
})
export class LoginComponent {
  loginForm: FormGroup;
  totpForm: FormGroup;
  loading = signal<boolean>(false);
  error = signal<string | null>(null);
  requires2FA = signal<boolean>(false);
  private tempToken = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
    });
    this.totpForm = this.fb.group({
      code: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]],
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
        if (response && response.requires2FA === 'true') {
          // 2FA required: show OTP form
          this.tempToken = response.tempToken;
          this.requires2FA.set(true);
          this.loading.set(false);
        } else if (response && response.token) {
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

  onVerify2FA(): void {
    if (this.totpForm.invalid) {
      this.totpForm.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    const code = this.totpForm.value.code;

    this.authService.verify2FA(this.tempToken, code).subscribe({
      next: (response) => {
        if (response && response.token) {
          this.authService.setToken(response.token);
          this.router.navigate(['/']);
        } else {
          this.error.set('Verification failed.');
          this.loading.set(false);
        }
      },
      error: (err) => {
        const errorMsg =
          err?.error?.error ||
          'Invalid code. Please try again.';
        this.error.set(errorMsg);
        this.loading.set(false);
      },
    });
  }

  back(event: Event): void {
    event.preventDefault();
    this.requires2FA.set(false);
    this.error.set(null);
    this.tempToken = '';
  }
}
