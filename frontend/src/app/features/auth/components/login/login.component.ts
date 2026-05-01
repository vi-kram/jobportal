import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthApiService } from '../../services/auth-api.service';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
<div class="min-h-screen bg-gray-100 flex flex-col items-center justify-center px-4">
  <div class="flex flex-col items-center mb-6">
    <div class="w-12 h-12 bg-blue-700 rounded-xl flex items-center justify-center mb-4">
      <span class="text-white text-xl font-bold">J</span>
    </div>
    <h1 class="text-2xl font-bold text-gray-900">Sign in</h1>
    <p class="text-gray-500 text-sm mt-1">Stay updated on your professional world</p>
  </div>

  <div class="bg-white rounded-2xl shadow-md w-full max-w-md p-8">
    <div class="bg-blue-50 border border-blue-200 rounded-lg p-3 mb-6 flex gap-2 text-sm text-blue-700">
      <span>ℹ️</span>
      <span>Welcome back! Your dashboard will automatically adapt whether you're a Job Seeker, Recruiter, or Admin.</span>
    </div>

    <div *ngIf="error" class="bg-red-50 border border-red-200 text-red-600 rounded-lg p-3 mb-4 text-sm">{{ error }}</div>

    <form [formGroup]="form" (ngSubmit)="onSubmit()">
      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700 mb-1">Email or Phone</label>
        <input formControlName="email" type="email" placeholder="Email or phone number"
          class="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
      </div>

      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700 mb-1">Password</label>
        <div class="relative">
          <input formControlName="password" [type]="showPassword ? 'text' : 'password'" placeholder="Password"
            class="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 pr-10" />
          <button type="button" (click)="togglePassword()" class="absolute right-3 top-2.5 text-gray-400 text-sm">
            {{ showPassword ? '🙈' : '👁️' }}
          </button>
        </div>
      </div>

      <div class="flex items-center justify-between mb-4">
        <label class="flex items-center gap-2 text-sm text-gray-600">
          <input type="checkbox" class="rounded" /> Remember me
        </label>
        <button type="button" (click)="showForgotMsg = !showForgotMsg" class="text-sm text-blue-600 font-medium hover:underline">
          Forgot password?
        </button>
      </div>

      <div *ngIf="showForgotMsg" class="bg-blue-50 border border-blue-200 text-blue-700 rounded-lg p-3 mb-4 text-sm">
        Please contact your administrator to reset your password.
      </div>

      <button type="submit" [disabled]="loading"
        class="w-full bg-blue-700 hover:bg-blue-800 text-white font-semibold py-3 rounded-full transition disabled:opacity-50">
        {{ loading ? 'Signing in...' : 'Sign in' }}
      </button>
    </form>

    <div class="flex items-center my-5">
      <div class="flex-1 border-t border-gray-200"></div>
      <span class="px-3 text-sm text-gray-400">or</span>
      <div class="flex-1 border-t border-gray-200"></div>
    </div>

    <div class="flex gap-3">
      <button type="button" (click)="showSocialMsg = true"
        class="flex-1 flex items-center justify-center gap-2 border border-gray-300 rounded-full py-2.5 text-sm font-medium hover:bg-gray-50 transition">
        <img src="https://www.svgrepo.com/show/475656/google-color.svg" class="w-4 h-4" alt="Google" />
        Google
      </button>
      <button type="button" (click)="showSocialMsg = true"
        class="flex-1 flex items-center justify-center gap-2 border border-gray-300 rounded-full py-2.5 text-sm font-medium hover:bg-gray-50 transition">
        🍎 Apple
      </button>
    </div>
    <div *ngIf="showSocialMsg" class="mt-3 text-center text-xs text-gray-400">Social login coming soon</div>
  </div>

  <p class="mt-6 text-sm text-gray-500">
    New to the platform?
    <a routerLink="/register" class="text-blue-600 font-semibold hover:underline">Join now</a>
  </p>
</div>
  `
})
export class LoginComponent {
  form: any;
  showPassword = false;
  showForgotMsg = false;
  showSocialMsg = false;
  error = '';
  loading = false;

  constructor(
    private fb: FormBuilder,
    private authApi: AuthApiService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  togglePassword() { this.showPassword = !this.showPassword; }

  onSubmit() {
    if (this.form.invalid) return;
    this.loading = true;
    this.error = '';
    const { email, password } = this.form.value;
    this.authApi.login(email!, password!).subscribe({
      next: (res) => {
        this.authService.saveToken(res.token);
        this.authService.redirectByRole();
      },
      error: () => {
        this.error = 'Invalid email or password';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }
}
