import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { NavbarComponent } from '../../../../shared/components/navbar/navbar.component';
import { FooterComponent } from '../../../../shared/components/footer/footer.component';
import { ResumeService } from '../../services/resume.service';
import { AuthService } from '../../../../core/services/auth.service';
import { ConfirmModalComponent } from '../../../../shared/components/confirm-modal/confirm-modal.component';

@Component({
  selector: 'app-my-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent, FooterComponent, ConfirmModalComponent],
  template: `
<div class="min-h-screen flex flex-col" style="background:var(--bg)">
  <app-navbar />

  <!-- Toast -->
  <div *ngIf="toastMessage" class="fixed top-6 left-1/2 -translate-x-1/2 z-50 shadow-xl rounded-2xl px-6 py-4 flex items-center gap-4 min-w-80" style="background:var(--surface); border:1px solid var(--accent)">
    <div class="w-10 h-10 rounded-full flex items-center justify-center text-xl shrink-0" style="background:#d4e6d4; color:#2d6a2d">✅</div>
    <div>
      <p class="font-semibold text-sm" style="color:var(--text)">{{ toastMessage }}</p>
      <p class="text-xs mt-0.5" style="color:var(--text)">{{ toastSub }}</p>
    </div>
    <button (click)="toastMessage = ''" class="ml-auto text-xl leading-none" style="color:var(--text)">&times;</button>
  </div>

  <main class="flex-1 max-w-3xl mx-auto w-full px-6 py-8">

    <div class="flex items-center gap-4 mb-8">
      <div class="w-16 h-16 rounded-full flex items-center justify-center text-white text-2xl font-bold shrink-0" style="background:var(--accent)">
        {{ initials }}
      </div>
      <div>
        <h1 class="text-2xl font-bold" style="color:var(--text)">{{ profile?.name || email }}</h1>
        <p class="text-sm" style="color:var(--text)">{{ email }}</p>
        <span class="text-xs font-semibold px-2.5 py-0.5 rounded-full mt-1 inline-block" style="background:var(--bg); color:var(--accent); border:1px solid var(--border)">{{ role }}</span>
      </div>
    </div>

    <div *ngIf="loading" class="flex justify-center py-20">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2" style="border-color:var(--accent)"></div>
    </div>

    <div *ngIf="!loading" class="space-y-5">

      <!-- Profile Completeness -->
      <div class="rounded-xl p-6" style="background:var(--surface); border:1px solid var(--border)">
        <div class="flex items-center justify-between mb-3">
          <h2 class="font-bold" style="color:var(--text)">Profile Completeness</h2>
          <span class="font-bold text-sm" style="color:var(--text)">{{ completeness.score }}%</span>
        </div>
        <div class="w-full rounded-full h-2.5 mb-3" style="background:var(--bg)">
          <div class="h-2.5 rounded-full transition-all duration-500" [style.width.%]="completeness.score" style="background:var(--accent)"></div>
        </div>
        <div *ngIf="completeness.missing.length > 0" class="flex flex-wrap gap-2">
          <span *ngFor="let item of completeness.missing" class="text-xs px-3 py-1 rounded-full flex items-center gap-1" style="background:var(--bg); border:1px solid var(--border); color:var(--accent)">⚠️ {{ item }}</span>
        </div>
        <p *ngIf="completeness.score === 100" class="text-sm font-medium" style="color:#2d6a2d">Your profile is complete!</p>
      </div>

      <div class="rounded-xl p-6" style="background:var(--surface); border:1px solid var(--border)">
        <h2 class="font-bold mb-5" style="color:var(--text)">Personal Information</h2>
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium mb-1" style="color:var(--text)">Full Name</label>
            <input [(ngModel)]="name" type="text" placeholder="Your full name"
              class="w-full rounded-lg px-3 py-2.5 text-sm focus:outline-none" style="border:1px solid var(--border); background:var(--bg); color:var(--text)" />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1" style="color:var(--text)">About Me</label>
            <input [(ngModel)]="headline" type="text" placeholder="e.g. Passionate developer with 3 years of experience..."
              class="w-full rounded-lg px-3 py-2.5 text-sm focus:outline-none" style="border:1px solid var(--border); background:var(--bg); color:var(--text)" />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1" style="color:var(--text)">Email Address</label>
            <input [value]="email" type="text" disabled
              class="w-full rounded-lg px-3 py-2.5 text-sm cursor-not-allowed" style="border:1px solid var(--border); background:#d8d8d0; color:var(--accent)" />
            <p class="text-xs mt-1" style="color:var(--text)">Email cannot be changed</p>
          </div>
          <div>
            <label class="block text-sm font-medium mb-1" style="color:var(--text)">Mobile Number</label>
            <input [(ngModel)]="mobile" type="tel" placeholder="e.g. +91 9876543210"
              class="w-full rounded-lg px-3 py-2.5 text-sm focus:outline-none" style="border:1px solid var(--border); background:var(--bg); color:var(--text)" />
          </div>
        </div>
        <button (click)="saveProfile()" [disabled]="saving"
          class="mt-5 text-white text-sm font-semibold px-6 py-2.5 rounded-full transition disabled:opacity-50" style="background:var(--accent)">
          {{ saving ? 'Saving...' : 'Save Changes' }}
        </button>
      </div>

      <!-- Skills -->
      <div class="rounded-xl p-6" style="background:var(--surface); border:1px solid var(--border)">
        <h2 class="font-bold mb-4" style="color:var(--text)">Skills</h2>
        <div class="flex gap-2 mb-3">
          <input [(ngModel)]="skillInput" type="text" placeholder="e.g. React, Java, Python" (keydown.enter)="addSkill()"
            class="flex-1 rounded-lg px-3 py-2.5 text-sm focus:outline-none" style="border:1px solid var(--border); background:var(--bg); color:var(--text)" />
          <button (click)="addSkill()" class="text-white px-4 py-2 rounded-lg text-sm font-medium" style="background:var(--accent)">Add</button>
        </div>
        <div *ngIf="skillList.length > 0" class="flex flex-wrap gap-2">
          <span *ngFor="let s of skillList" class="text-xs px-3 py-1.5 rounded-full flex items-center gap-2" style="background:var(--bg); border:1px solid var(--border); color:var(--accent)">
            {{ s }}
            <button (click)="removeSkill(s)" class="leading-none" style="color:var(--text)">&times;</button>
          </span>
        </div>
        <p *ngIf="skillList.length === 0" class="text-sm" style="color:var(--text)">No skills added yet.</p>
        <button (click)="saveSkills()" [disabled]="!skillsDirty"
          class="mt-4 text-white text-sm font-semibold px-6 py-2.5 rounded-full transition disabled:opacity-50" style="background:var(--accent)">
          Save Skills
        </button>
      </div>

      <!-- Quick Links -->
      <div class="rounded-xl p-6" style="background:var(--surface); border:1px solid var(--border)">
        <h2 class="font-bold mb-4" style="color:var(--text)">Quick Links</h2>
        <div class="grid grid-cols-2 gap-3">
          <button (click)="router.navigate(['/resume'])" class="flex items-center gap-3 p-4 rounded-xl transition text-left" style="border:1px solid var(--border); background:var(--bg)">
            <span class="text-2xl">📄</span>
            <div><p class="text-sm font-semibold" style="color:var(--text)">Resume</p><p class="text-xs" style="color:var(--text)">Manage your resume</p></div>
          </button>
          <button (click)="router.navigate(['/applications'])" class="flex items-center gap-3 p-4 rounded-xl transition text-left" style="border:1px solid var(--border); background:var(--bg)">
            <span class="text-2xl">📋</span>
            <div><p class="text-sm font-semibold" style="color:var(--text)">Applications</p><p class="text-xs" style="color:var(--text)">Track your applications</p></div>
          </button>
          <button (click)="router.navigate(['/change-password'])" class="flex items-center gap-3 p-4 rounded-xl transition text-left" style="border:1px solid var(--border); background:var(--bg)">
            <span class="text-2xl">🔒</span>
            <div><p class="text-sm font-semibold" style="color:var(--text)">Change Password</p><p class="text-xs" style="color:var(--text)">Update your password</p></div>
          </button>
          <button (click)="router.navigate(['/jobs'])" class="flex items-center gap-3 p-4 rounded-xl transition text-left" style="border:1px solid var(--border); background:var(--bg)">
            <span class="text-2xl">💼</span>
            <div><p class="text-sm font-semibold" style="color:var(--text)">Browse Jobs</p><p class="text-xs" style="color:var(--text)">Find your next role</p></div>
          </button>
        </div>
      </div>

      <!-- Account -->
      <div class="rounded-xl p-6" style="background:var(--surface); border:1px solid var(--border)">
        <h2 class="font-bold mb-1" style="color:var(--text)">Account</h2>
        <p class="text-xs mb-4" style="color:var(--text)">Manage your account session</p>
        <button (click)="showLogoutModal = true"
          class="flex items-center gap-2 px-5 py-2.5 rounded-full text-sm font-medium transition" style="border:1px solid #d9a0a0; color:var(--danger)">
          🚪 Sign Out
        </button>
      </div>

    </div>
  </main>
  <app-footer />
</div>

<app-confirm-modal
  [visible]="showLogoutModal"
  (confirmed)="doLogout()"
  (cancelled)="showLogoutModal = false">
</app-confirm-modal>
  `
})
export class MyProfileComponent implements OnInit {

  profile: any = null;
  loading = true;
  saving = false;
  name = '';
  toastMessage = '';
  toastSub = '';
  resumes: any[] = [];
  headline = '';
  skills = '';
  mobile = '';
  skillList: string[] = [];
  skillInput = '';
  skillsDirty = false;

  constructor(
    public router: Router,
    private resumeService: ResumeService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  get email() { return this.authService.getEmail() || ''; }
  get role() { return this.authService.getRole() || ''; }
  get initials() { return this.email ? this.email.substring(0, 2).toUpperCase() : 'U'; }

  get completeness(): { score: number; missing: string[] } {
    const checks = [
      { label: 'Full name', done: !!this.name.trim() },
      { label: 'About Me', done: !!this.headline.trim() },
      { label: 'Skills', done: this.skillList.length > 0 },
      { label: 'Resume uploaded', done: this.resumes.length > 0 },
    ];
    const done = checks.filter(c => c.done).length;
    return { score: Math.round((done / checks.length) * 100), missing: checks.filter(c => !c.done).map(c => c.label) };
  }

  ngOnInit() {
    this.resumeService.getProfile().subscribe({
      next: (res) => {
        this.profile = res;
        this.name = res.name || '';
        this.headline = res.headline || '';
        this.skills = res.skills || '';
        this.skillList = this.skills ? this.skills.split(',').map((s: string) => s.trim()).filter(Boolean) : [];
        this.mobile = res.mobile || '';
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => { this.loading = false; this.cdr.detectChanges(); }
    });
    this.resumeService.getMyResumes().subscribe({
      next: (res) => { this.resumes = Array.isArray(res) ? res : (res.content || []); this.cdr.detectChanges(); },
      error: () => {}
    });
  }

  saveProfile() {
    if (!this.profile || !this.name.trim()) return;
    this.saving = true;
    this.resumeService.updateProfile(this.profile.id, {
      name: this.name.trim(),
      mobile: this.mobile.trim(),
      headline: this.headline.trim(),
      skills: this.skillList.join(', ')
    }).subscribe({
      next: () => {
        this.saving = false;
        this.profile.name = this.name.trim();
        this.showToast('Profile updated!', 'Your details have been saved.');
      },
      error: () => { this.saving = false; this.cdr.detectChanges(); }
    });
  }

  addSkill() {
    const s = this.skillInput.trim();
    if (!s || this.skillList.includes(s)) return;
    this.skillList = [...this.skillList, s];
    this.skillInput = '';
    this.skillsDirty = true;
    this.cdr.detectChanges();
  }

  removeSkill(skill: string) {
    this.skillList = this.skillList.filter(s => s !== skill);
    this.skillsDirty = true;
    this.cdr.detectChanges();
  }

  saveSkills() {
    if (!this.profile) return;
    this.resumeService.updateProfile(this.profile.id, {
      name: this.name.trim() || this.profile.name,
      mobile: this.mobile.trim(),
      headline: this.headline.trim(),
      skills: this.skillList.join(', ')
    }).subscribe({
      next: () => {
        this.skillsDirty = false;
        this.showToast('Skills saved!', 'Your skills have been updated.');
      },
      error: () => this.cdr.detectChanges()
    });
  }

  showToast(message: string, sub: string) {
    this.toastMessage = message;
    this.toastSub = sub;
    this.cdr.detectChanges();
    setTimeout(() => { this.toastMessage = ''; this.cdr.detectChanges(); }, 4000);
  }

  showLogoutModal = false;
  doLogout() { this.authService.logout(); }
}
