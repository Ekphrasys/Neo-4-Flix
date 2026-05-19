import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import QRCode from 'qrcode';

import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-two-factor-setup',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
  <section class="auth" aria-label="Two-Factor Authentication Setup">
    <div class="auth__card">
      <header class="auth__header">
        <h1 class="auth__title">Enable 2FA</h1>
        <p class="auth__subtitle">Secure your account with Google Authenticator</p>
      </header>

      <!-- Step 1: Generate QR Code -->
      @if (step() === 'init') {
        <div class="setup">
          <p class="setup__description">
            Two-factor authentication adds an extra layer of security to your account.
            You'll need the Google Authenticator app on your phone.
          </p>
          <button class="btn btn--accent" (click)="startSetup()" [disabled]="loading()">
            @if (loading()) {
              <span class="spinner" aria-hidden="true"></span>
              Generating...
            } @else {
              Generate QR Code
            }
          </button>
        </div>
      }

      <!-- Step 2: Scan QR Code & Confirm -->
      @if (step() === 'scan') {
        <div class="setup">
          <p class="setup__description">
            Scan this QR code with Google Authenticator, then enter the 6-digit code below to confirm.
          </p>

          <div class="setup__qr">
            <img [src]="qrImageUrl()" alt="QR Code for 2FA setup" class="setup__qr-img" />
          </div>

          <p class="setup__manual">
            Can't scan? Enter this key manually:<br/>
            <code class="setup__secret">{{ secret() }}</code>
          </p>

          <div class="field">
            <label class="field__label" for="confirm-code">Confirmation Code</label>
            <input
              class="field__control field__control--otp"
              type="text"
              [(ngModel)]="confirmCode"
              id="confirm-code"
              autocomplete="one-time-code"
              placeholder="000000"
              maxlength="6"
              inputmode="numeric"
            />
          </div>

          @if (error()) {
            <p class="alert" role="alert">{{ error() }}</p>
          }

          <button class="btn btn--accent" (click)="confirmSetup()" [disabled]="loading()">
            @if (loading()) {
              <span class="spinner" aria-hidden="true"></span>
              Verifying...
            } @else {
              Activate 2FA
            }
          </button>
        </div>
      }

      <!-- Step 3: Success -->
      @if (step() === 'done') {
        <div class="setup setup--done">
          <div class="setup__check">✓</div>
          <p class="setup__description">
            Two-factor authentication is now <strong>enabled</strong> on your account.
            You will be asked for a code from Google Authenticator each time you sign in.
          </p>
          <button class="btn btn--accent" (click)="goHome()">Back to Home</button>
        </div>
      }
    </div>
  </section>
  `,
  styleUrl: './two-factor-setup.component.css',
})
export class TwoFactorSetupComponent {
  step = signal<'init' | 'scan' | 'done'>('init');
  loading = signal(false);
  error = signal<string | null>(null);
  secret = signal('');
  qrImageUrl = signal('');
  confirmCode = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  startSetup(): void {
    this.loading.set(true);
    this.error.set(null);

    this.authService.setup2FA().subscribe({
      next: (res) => {
        this.secret.set(res.secret);
        // Generate QR code client-side using the qrcode library
        QRCode.toDataURL(res.qrCodeUri, { width: 250, margin: 2 })
          .then((dataUrl: string) => {
            this.qrImageUrl.set(dataUrl);
            this.step.set('scan');
            this.loading.set(false);
          })
          .catch(() => {
            this.error.set('Failed to generate QR code.');
            this.loading.set(false);
          });
      },
      error: (err) => {
        this.error.set(err?.error?.error || 'Failed to setup 2FA. Please try again.');
        this.loading.set(false);
      },
    });
  }

  confirmSetup(): void {
    if (!this.confirmCode || this.confirmCode.length !== 6) {
      this.error.set('Please enter the 6-digit code from your authenticator app.');
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    this.authService.confirm2FA(this.confirmCode).subscribe({
      next: () => {
        this.step.set('done');
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err?.error?.error || 'Invalid code. Please try again.');
        this.loading.set(false);
      },
    });
  }

  goHome(): void {
    this.router.navigate(['/']);
  }
}
