import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
  <section class="auth" aria-label="Registration">
    <div class="auth__card">
      <header class="auth__header">
        <h1 class="auth__title">Register an account</h1>
      </header>

      <form class="form" (submit)="register($event)" novalidate>
        <div class="field">
          <label class="field__label" for="name">Username</label>
          <input
            class="field__control"
            id="name"
            name="name"
            type="text"
            autocomplete="name"
            required
            [(ngModel)]="name"
            placeholder="Username..."
          />
        </div>

        <div class="field">
          <label class="field__label" for="email">Email</label>
          <input
            class="field__control"
            id="email"
            name="email"
            type="email"
            autocomplete="email"
            required
            [(ngModel)]="email"
            placeholder="Email..."
          />
        </div>

        <div class="field">
          <label class="field__label" for="password">Password</label>
          <input
            class="field__control"
            id="password"
            name="password"
            type="password"
            autocomplete="new-password"
            minlength="8"
            required
            [(ngModel)]="password"
            (ngModelChange)="validatePassword()"
            placeholder="••••••••"
          />
          @if (password.length > 0) {
            <ul class="password-rules">
              <li [class.pass]="rules.minLength" [class.fail]="!rules.minLength">
                {{ rules.minLength ? '✓' : '✗' }} At least 8 characters
              </li>
              <li [class.pass]="rules.uppercase" [class.fail]="!rules.uppercase">
                {{ rules.uppercase ? '✓' : '✗' }} One uppercase letter
              </li>
              <li [class.pass]="rules.lowercase" [class.fail]="!rules.lowercase">
                {{ rules.lowercase ? '✓' : '✗' }} One lowercase letter
              </li>
              <li [class.pass]="rules.digit" [class.fail]="!rules.digit">
                {{ rules.digit ? '✓' : '✗' }} One digit
              </li>
              <li [class.pass]="rules.special" [class.fail]="!rules.special">
                {{ rules.special ? '✓' : '✗' }} One special character
              </li>
            </ul>
          }
        </div>

        @if (error) {
          <p class="alert" role="alert">{{ error }}</p>
        }

        <button type="submit" class="btn btn--accent form__submit"
          [disabled]="!allRulesPass">Create an account</button>

        <p class="auth__footer">
          <a routerLink="/login">Already have an account ? Sign in</a>
        </p>
      </form>
    </div>
  </section>`,
  styleUrl: './register.component.css',
})

export class RegisterComponent {
  name = ''; email = ''; password = ''; error = '';

  rules = {
    minLength: false,
    uppercase: false,
    lowercase: false,
    digit: false,
    special: false,
  };

  get allRulesPass(): boolean {
    return this.rules.minLength && this.rules.uppercase
      && this.rules.lowercase && this.rules.digit && this.rules.special;
  }

  constructor(private auth: AuthService, private router: Router) {}

  validatePassword(): void {
    const p = this.password;
    this.rules.minLength = p.length >= 8;
    this.rules.uppercase = /[A-Z]/.test(p);
    this.rules.lowercase = /[a-z]/.test(p);
    this.rules.digit     = /[0-9]/.test(p);
    this.rules.special   = /[^a-zA-Z0-9]/.test(p);
  }

  register(evt: Event) {
    evt.preventDefault();
    this.error = '';
    if (!this.allRulesPass) {
      this.error = 'Please fix the password requirements above';
      return;
    }
    this.auth.register({ name: this.name, email: this.email, password: this.password }).subscribe({
      next: res => { this.auth.setToken(res.token); this.router.navigate(['/']); },
      error: err => { this.error = err.error?.error || 'Register failed'; }
    });
  }
}
