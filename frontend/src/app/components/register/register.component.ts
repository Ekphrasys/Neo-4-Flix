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
            minlength="6"
            required
            [(ngModel)]="password"
            placeholder="••••••••"
          />
        </div>

        @if (error) {
          <p class="alert" role="alert">{{ error }}</p>
        }

        <button type="submit" class="btn btn--accent form__submit">Create an account</button>

        <p class="auth__footer">
          <a routerLink="/login">Already have an account ? Sign in</a>
        </p>
      </form>
    </div>
  </section>`,
  styleUrl: './register.component.css',
})

export class RegisterComponent {
  name=''; email=''; password=''; error='';
  constructor(private auth: AuthService, private router: Router) {}

  register(evt: Event) {
    evt.preventDefault();
    this.error='';
    this.auth.register({ name: this.name, email: this.email, password: this.password }).subscribe({
      next: res => { this.auth.setToken(res.token); this.router.navigate(['/']); },
      error: err => { this.error = err.error?.error || 'Register failed'; }
    });
  }
}
