import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { NavbarComponent } from '../../../../shared/components/navbar/navbar.component';
import { FooterComponent } from '../../../../shared/components/footer/footer.component';
import { AuthService } from '../../../../core/services/auth.service';
import { ConfirmModalComponent } from '../../../../shared/components/confirm-modal/confirm-modal.component';
import { environment } from '../../../../../environments/environment';
@Component({
  selector: 'app-recruiter-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent, FooterComponent, ConfirmModalComponent],
  template: `
<div class="min-h-screen flex flex-col" style="background:var(--bg)">
  <app-navbar />

  <!-- Toast -->
  <div *ngIf="toastMessage" class="fixed top-6 left-1/2 -translate-x-1/2 z-50 shadow-xl rounded-2xl px-6 py-4 flex items-center gap-4 min-w-80" style="background:var(--surface); border:1px solid var(--border)">
    <div class="w-10 h-10 rounded-full flex items-center justify-center text-xl shrink-0" style="background:var(--accent-dim); color:var(--accent)">✅</div>
    <div>
      <p class="font-semibold text-sm" style="color:var(--text)">{{ toastMessage }}</p>
      <p class="text-xs mt-0.5" style="color:var(--text-muted)">Your profile has been updated.</p>
    </div>
    <button (click)="toastMessage = ''" class="ml-auto text-xl leading-none" style="color:var(--text-muted)">&times;</button>
  </div>

  <main class="flex-1 max-w-3xl mx-auto w-full px-6 py-8">

    <!-- Avatar Header -->
    <div class="flex items-center gap-4 mb-8">
      <div class="w-16 h-16 rounded-full flex items-center justify-center text-white text-2xl font-bold shrink-0 avatar-gradient">
        {{ initials }}
      </div>
      <div>
        <h1 class="text-2xl font-bold" style="color:var(--text)">{{ name || email }}</h1>
        <p class="text-sm" style="color:var(--text-muted)">{{ email }}</p>
        <span class="badge mt-1 inline-block" style="background:var(--accent-dim); color:var(--accent)">RECRUITER</span>
      </div>
    </div>

    <div class="space-y-5">

      <!-- Personal Information -->
      <div class="rounded-xl p-6" style="background:var(--surface); border:1px solid var(--border)">
        <h2 class="font-bold mb-5" style="color:var(--text)">Personal Information</h2>
        <div class="space-y-4">
          <div>
            <label class="block text-xs font-bold uppercase tracking-widest mb-2" style="color:var(--text-muted)">Full Name</label>
            <div class="flex items-center rounded-lg px-3 py-2.5 gap-2" style="border:1px solid var(--border); background:var(--bg)">
              <input [(ngModel)]="name" type="text" placeholder="Your full name"
                class="text-sm outline-none w-full bg-transparent" style="color:var(--text)" />
            </div>
          </div>
          <div>
            <label class="block text-xs font-bold uppercase tracking-widest mb-2" style="color:var(--text-muted)">Email Address</label>
            <div class="flex items-center rounded-lg px-3 py-2.5 gap-2" style="border:1px solid var(--border); background:var(--surface-alt)">
              <input [value]="email" type="text" disabled
                class="text-sm outline-none w-full bg-transparent cursor-not-allowed" style="color:var(--text-muted)" />
            </div>
            <p class="text-xs mt-1" style="color:var(--text-muted)">Email cannot be changed</p>
          </div>
          <div>
            <label class="block text-xs font-bold uppercase tracking-widest mb-2" style="color:var(--text-muted)">Mobile Number</label>
            <div class="flex items-center rounded-lg px-3 py-2.5 gap-2" style="border:1px solid var(--border); background:var(--bg)">
              <input [(ngModel)]="mobile" type="tel" placeholder="e.g. +91 9876543210"
                class="text-sm outline-none w-full bg-transparent" style="color:var(--text)" />
            </div>
          </div>
        </div>
        <button (click)="saveProfile()" [disabled]="saving"
          class="btn-primary mt-5 disabled:opacity-50">
          {{ saving ? 'Saving...' : 'Save Changes' }}
        </button>
      </div>

      <!-- Quick Links -->
      <div class="rounded-xl p-6" style="background:var(--surface); border:1px solid var(--border)">
        <h2 class="font-bold mb-4" style="color:var(--text)">Quick Links</h2>
        <div class="grid grid-cols-2 gap-3">
          <button (click)="router.navigate(['/recruiter/dashboard'])" class="job-card flex items-center gap-3 p-4 rounded-xl transition text-left" style="background:var(--bg); border:1px solid var(--border)">
            <span class="text-2xl">📊</span>
            <div>
              <p class="text-sm font-bold" style="color:var(--text)">Dashboard</p>
              <p class="text-xs" style="color:var(--text-muted)">View your overview</p>
            </div>
          </button>
          <button (click)="router.navigate(['/recruiter/my-jobs'])" class="job-card flex items-center gap-3 p-4 rounded-xl transition text-left" style="background:var(--bg); border:1px solid var(--border)">
            <span class="text-2xl">💼</span>
            <div>
              <p class="text-sm font-bold" style="color:var(--text)">My Jobs</p>
              <p class="text-xs" style="color:var(--text-muted)">Manage your postings</p>
            </div>
          </button>
          <button (click)="router.navigate(['/recruiter/applicants/0'])" class="job-card flex items-center gap-3 p-4 rounded-xl transition text-left" style="background:var(--bg); border:1px solid var(--border)">
            <span class="text-2xl">👥</span>
            <div>
              <p class="text-sm font-bold" style="color:var(--text)">Applicants</p>
              <p class="text-xs" style="color:var(--text-muted)">Review candidates</p>
            </div>
          </button>
          <button (click)="router.navigate(['/change-password'])" class="job-card flex items-center gap-3 p-4 rounded-xl transition text-left" style="background:var(--bg); border:1px solid var(--border)">
            <span class="text-2xl">🔒</span>
            <div>
              <p class="text-sm font-bold" style="color:var(--text)">Change Password</p>
              <p class="text-xs" style="color:var(--text-muted)">Update your password</p>
            </div>
          </button>
        </div>
      </div>

      <!-- Account -->
      <div class="rounded-xl p-6" style="background:var(--surface); border:1px solid var(--border)">
        <h2 class="font-bold mb-1" style="color:var(--text)">Account</h2>
        <p class="text-xs mb-4" style="color:var(--text-muted)">Manage your account session</p>
        <button (click)="showLogoutModal = true"
          class="flex items-center gap-2 px-5 py-2.5 rounded-full text-sm font-semibold transition"
          style="border:1px solid var(--danger); color:var(--danger)">
          🚪 Sign Out
        </button>
      </div>

    </div>
  </main>
  <app-footer />
</div>

<app-confirm-modal
  [visible]="showLogoutModal"
  (confirmed)="authService.logout()"
  (cancelled)="showLogoutModal = false">
</app-confirm-modal>
  `
})
export class RecruiterProfileComponent implements OnInit {

  name = '';
  mobile = '';
  saving = false;
  toastMessage = '';

  constructor(
    public router: Router,
    public authService: AuthService,
    private http: HttpClient,
    private cdr: ChangeDetectorRef
  ) {}

  showLogoutModal = false;
  get email() { return this.authService.getEmail() || ''; }
  get initials() { return this.email ? this.email.substring(0, 2).toUpperCase() : 'R'; }
  confirmLogout() { this.showLogoutModal = true; }

  ngOnInit() {
    this.http.get<any>(`${environment.apiUrl}/api/users/me`).subscribe({
      next: (res) => { this.name = res.name || ''; this.mobile = res.mobile || ''; this.cdr.detectChanges(); },
      error: () => {}
    });
  }

  saveProfile() {
    if (!this.name.trim()) return;
    this.saving = true;
    this.http.get<any>(`${environment.apiUrl}/api/users/me`).subscribe({
      next: (user) => {
        this.http.put<any>(`${environment.apiUrl}/api/users/${user.id}`, {
          name: this.name.trim(),
          mobile: this.mobile.trim()
        }).subscribe({
          next: () => {
            this.saving = false;
            this.toastMessage = 'Profile updated!';
            this.cdr.detectChanges();
            setTimeout(() => { this.toastMessage = ''; this.cdr.detectChanges(); }, 4000);
          },
          error: () => { this.saving = false; this.cdr.detectChanges(); }
        });
      }
    });
  }
}
