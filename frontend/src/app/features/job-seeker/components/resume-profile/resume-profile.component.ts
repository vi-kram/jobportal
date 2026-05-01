import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../../../shared/components/navbar/navbar.component';
import { FooterComponent } from '../../../../shared/components/footer/footer.component';
import { ResumeService } from '../../services/resume.service';
import { AuthService } from '../../../../core/services/auth.service';
import { AiService } from '../../services/ai.service';

@Component({
  selector: 'app-resume-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent, FooterComponent],
  template: `
<div class="min-h-screen flex flex-col" style="background:var(--bg)">
  <app-navbar />

  <!-- Toast -->
  <div *ngIf="toastMessage" class="fixed top-6 left-1/2 -translate-x-1/2 z-50 shadow-xl rounded-2xl px-6 py-4 flex items-center gap-4 min-w-80" style="background:var(--surface); border:1px solid var(--border)">
    <div class="w-10 h-10 rounded-full flex items-center justify-center text-xl shrink-0" style="background:var(--accent-dim); color:var(--accent)">✅</div>
    <div>
      <p class="font-semibold text-sm" style="color:var(--text)">{{ toastMessage }}</p>
      <p class="text-xs mt-0.5" style="color:var(--accent)">{{ toastSub }}</p>
    </div>
    <button (click)="toastMessage = ''" class="ml-auto text-xl leading-none" style="color:var(--accent)">&times;</button>
  </div>

  <main class="flex-1 max-w-6xl mx-auto w-full px-6 py-8">
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-2xl font-bold" style="color:var(--text)">Resume &amp; Profile</h1>
        <p class="text-sm mt-1" style="color:var(--text)">Manage your professional details and application documents.</p>
      </div>
      <button class="px-4 py-2 rounded-full text-sm font-medium transition" style="border:1px solid var(--border); color:var(--text); background:var(--surface)">View Public Profile</button>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">

      <!-- Left — Profile Info -->
      <div class="rounded-xl p-6" style="background:var(--surface); border:1px solid var(--border)">
        <div class="flex items-center justify-between mb-5">
          <h2 class="font-bold" style="color:var(--text)">Profile Information</h2>
          <button (click)="saveProfile()" [disabled]="saving" class="text-sm font-medium hover:underline disabled:opacity-50" style="color:var(--accent)">
            {{ saving ? 'Saving...' : 'Save Changes' }}
          </button>
        </div>
        <div class="mb-4">
          <label class="block text-sm font-medium mb-1" style="color:var(--text)">Professional Headline</label>
          <input [(ngModel)]="headline" type="text" placeholder="e.g. Senior Frontend Developer | React | TypeScript"
            class="w-full rounded-lg px-3 py-2.5 text-sm focus:outline-none"
            style="border:1px solid var(--border); background:var(--bg); color:var(--text)" />
        </div>
        <div class="mb-4">
          <label class="block text-sm font-medium mb-1" style="color:var(--text)">Professional Summary</label>
          <textarea [(ngModel)]="summary" rows="4" placeholder="Write a brief professional summary..."
            class="w-full rounded-lg px-3 py-2.5 text-sm focus:outline-none resize-none"
            style="border:1px solid var(--border); background:var(--bg); color:var(--text)"></textarea>
        </div>
        <div>
          <label class="block text-sm font-medium mb-1" style="color:var(--text)">Top Skills</label>
          <input [(ngModel)]="skills" type="text" placeholder="e.g. React, TypeScript, Node.js"
            class="w-full rounded-lg px-3 py-2.5 text-sm focus:outline-none mb-2"
            style="border:1px solid var(--border); background:var(--bg); color:var(--text)" />
          <div *ngIf="skills" class="flex flex-wrap gap-2">
            <span *ngFor="let skill of skills.split(',')" class="text-xs px-3 py-1 rounded-full font-medium"
              style="background:var(--bg); color:var(--accent); border:1px solid var(--border)">{{ skill.trim() }}</span>
          </div>
        </div>
      </div>

      <!-- Right Column -->
      <div class="space-y-5">

        <!-- Current Resume -->
        <div class="rounded-xl p-6" style="background:var(--surface); border:1px solid var(--border)">
          <div class="flex items-center justify-between mb-4">
            <h2 class="font-bold" style="color:var(--text)">Current Resume</h2>
            <span *ngIf="activeResume" class="text-xs font-semibold px-2.5 py-1 rounded-full" style="background:var(--accent-dim); color:var(--accent)">ACTIVE</span>
          </div>

          <div *ngIf="activeResume" class="flex items-center justify-between p-3 rounded-lg mb-4" style="background:var(--bg); border:1px solid var(--border)">
            <div class="flex items-center gap-3 cursor-pointer" (click)="openResume(activeResume.fileUrl)">
              <span class="text-xl">📄</span>
              <div>
                <p class="text-sm font-medium hover:underline" style="color:var(--accent)">{{ getFileName(activeResume.fileUrl) }}</p>
                <p class="text-xs" style="color:var(--text)">Uploaded {{ activeResume.uploadedAt | date:'MMM dd, yyyy' }}</p>
              </div>
            </div>
            <div class="flex gap-2">
              <button (click)="analyzeResume()" [disabled]="analyzing"
                class="text-white text-xs px-3 py-1.5 rounded-full font-medium disabled:opacity-50 flex items-center gap-1"
                style="background:var(--accent)">
                {{ analyzing ? 'Analyzing...' : '🤖 Analyze' }}
              </button>
              <button (click)="deleteResume(activeResume.resumeId)" class="text-sm" style="color:var(--danger)">🗑</button>
            </div>
          </div>

          <!-- AI Analysis Result -->
          <div *ngIf="analysisResult" class="mt-3 rounded-xl overflow-hidden" style="border:1px solid var(--border)">
            <div class="px-4 py-3 flex items-center justify-between" style="background:var(--accent)">
              <span class="text-white font-semibold text-sm">🤖 AI Resume Analysis</span>
              <button (click)="analysisResult = null" class="text-white opacity-70 hover:opacity-100">&times;</button>
            </div>
            <div class="p-4 space-y-4" style="background:var(--bg)">
              <div class="flex items-center gap-4">
                <div class="w-16 h-16 rounded-full flex items-center justify-center text-xl font-bold shrink-0"
                  [style]="analysisResult.score >= 75 ? 'background:var(--accent-dim); color:var(--accent)' : analysisResult.score >= 50 ? 'background:var(--warn-dim); color:var(--warn-text)' : 'background:var(--danger-dim); color:var(--danger)'">
                  {{ analysisResult.score }}
                </div>
                <div>
                  <p class="text-sm font-semibold" style="color:var(--text)">Resume Score</p>
                  <p class="text-xs" style="color:var(--text)">{{ analysisResult.summary }}</p>
                </div>
              </div>
              <div *ngIf="analysisResult.strengths?.length">
                <p class="text-xs font-semibold uppercase mb-2" style="color:var(--accent)">✅ Strengths</p>
                <ul class="space-y-1">
                  <li *ngFor="let s of analysisResult.strengths" class="text-xs flex gap-2" style="color:var(--text)">
                    <span style="color:var(--accent)">•</span>{{ s }}
                  </li>
                </ul>
              </div>
              <div *ngIf="analysisResult.improvements?.length">
                <p class="text-xs font-semibold uppercase mb-2" style="color:var(--warn-text)">⚠️ Improvements</p>
                <ul class="space-y-1">
                  <li *ngFor="let i of analysisResult.improvements" class="text-xs flex gap-2" style="color:var(--text)">
                    <span style="color:var(--warn-text)">•</span>{{ i }}
                  </li>
                </ul>
              </div>
              <div *ngIf="analysisResult.missingKeywords?.length">
                <p class="text-xs font-semibold uppercase mb-2" style="color:var(--danger)">🔍 Missing Keywords</p>
                <div class="flex flex-wrap gap-1">
                  <span *ngFor="let k of analysisResult.missingKeywords" class="text-xs px-2 py-0.5 rounded-full"
                    style="background:var(--danger-dim); border:1px solid #d9a0a0; color:var(--danger)">{{ k }}</span>
                </div>
              </div>
              <div *ngIf="analysisResult.recommendation" class="rounded-lg p-3" style="background:var(--surface); border:1px solid var(--border)">
                <p class="text-xs font-semibold mb-1" style="color:var(--accent)">💡 Recommendation</p>
                <p class="text-xs" style="color:var(--text)">{{ analysisResult.recommendation }}</p>
              </div>
            </div>
          </div>

          <div *ngIf="!activeResume" class="text-center py-4 text-sm" style="color:var(--text)">No resume uploaded yet</div>
        </div>

        <!-- Upload New Resume -->
        <div class="rounded-xl p-6" style="background:var(--surface); border:1px solid var(--border)">
          <h2 class="font-bold mb-4" style="color:var(--text)">Upload New Resume</h2>
          <label class="block rounded-xl p-8 text-center cursor-pointer transition" style="border:2px dashed var(--accent)">
            <input type="file" accept=".pdf,.doc,.docx" (change)="onFileSelected($event)" class="hidden" />
            <div *ngIf="!uploading">
              <div class="text-3xl mb-2">⬆️</div>
              <p class="text-sm font-medium" style="color:var(--text)">Click to upload or drag and drop</p>
              <p class="text-xs mt-1" style="color:var(--accent)">PDF or DOCX (Max. 5MB)</p>
            </div>
            <div *ngIf="uploading" class="flex items-center justify-center gap-2">
              <div class="animate-spin rounded-full h-5 w-5 border-b-2" style="border-color:var(--accent)"></div>
              <span class="text-sm" style="color:var(--accent)">Uploading...</span>
            </div>
          </label>
        </div>

        <!-- Version History -->
        <div *ngIf="historyResumes.length > 0" class="rounded-xl p-6" style="background:var(--surface); border:1px solid var(--border)">
          <div class="flex items-center justify-between mb-4">
            <h2 class="font-bold" style="color:var(--text)">Version History</h2>
            <span class="text-xs" style="color:var(--accent)">Past {{ historyResumes.length }} uploads</span>
          </div>
          <div class="space-y-3">
            <div *ngFor="let r of historyResumes" class="flex items-center justify-between">
              <div class="flex items-center gap-3 cursor-pointer" (click)="openResume(r.fileUrl)">
                <span style="color:var(--accent)">📄</span>
                <div>
                  <p class="text-sm hover:underline" style="color:var(--accent)">{{ getFileName(r.fileUrl) }}</p>
                  <p class="text-xs" style="color:var(--text)">{{ r.uploadedAt | date:'MMM dd, yyyy' }}</p>
                </div>
              </div>
              <button (click)="deleteResume(r.resumeId)" style="color:var(--danger)">🗑</button>
            </div>
          </div>
        </div>

      </div>
    </div>
  </main>
  <app-footer />
</div>
  `
})
export class ResumeProfileComponent implements OnInit {

  profile: any = null;
  resumes: any[] = [];
  loading = true;
  saving = false;
  uploading = false;
  toastMessage = '';
  toastSub = '';

  headline = '';
  summary = '';
  skills = '';

  analyzing = false;
  analysisResult: any = null;

  constructor(
    private resumeService: ResumeService,
    private authService: AuthService,
    private aiService: AiService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadProfile();
    this.loadResumes();
  }

  showToast(message: string, sub: string) {
    this.toastMessage = message;
    this.toastSub = sub;
    this.cdr.detectChanges();
    setTimeout(() => { this.toastMessage = ''; this.cdr.detectChanges(); }, 4000);
  }

  loadProfile() {
    this.resumeService.getProfile().subscribe({
      next: (res) => {
        this.profile = res;
        this.headline = res.name || '';
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  loadResumes() {
    this.resumeService.getMyResumes().subscribe({
      next: (res) => {
        this.resumes = Array.isArray(res) ? res : (res.content || []);
        this.cdr.detectChanges();
      },
      error: () => { this.cdr.detectChanges(); }
    });
  }

  saveProfile() {
    if (!this.profile) return;
    const nameToSave = (this.headline || this.profile.name || '').trim();
    if (!nameToSave) return;
    this.saving = true;
    this.resumeService.updateProfile(this.profile.id, { name: nameToSave }).subscribe({
      next: () => {
        this.saving = false;
        this.profile.name = nameToSave;
        this.showToast('Profile saved successfully!', 'Your profile has been updated.');
      },
      error: () => {
        this.saving = false;
        this.cdr.detectChanges();
      }
    });
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (!file) return;
    this.uploading = true;
    this.resumeService.uploadResumeFile(file).subscribe({
      next: (res) => {
        this.resumes = [res, ...this.resumes];
        this.uploading = false;
        this.showToast('Resume uploaded successfully!', this.getFileName(res.fileUrl) + ' is now active.');
      },
      error: () => {
        this.uploading = false;
        this.cdr.detectChanges();
      }
    });
  }

  deleteResume(id: number) {
    this.resumeService.deleteResume(id).subscribe({
      next: () => {
        this.resumes = this.resumes.filter(r => r.resumeId !== id);
        this.cdr.detectChanges();
      }
    });
  }

  analyzeResume() {
    if (!this.activeResume || this.analyzing) return;
    this.analyzing = true;
    this.analysisResult = null;
    this.aiService.analyzeResume(this.activeResume.fileUrl).subscribe({
      next: (res) => { this.analysisResult = res; this.analyzing = false; this.cdr.detectChanges(); },
      error: () => { this.analyzing = false; this.showToast('Analysis failed', 'Could not analyze resume.'); }
    });
  }

  get activeResume() { return this.resumes[0] || null; }
  get historyResumes() { return this.resumes.slice(1); }

  getFileName(fileUrl: string): string {
    if (!fileUrl) return 'Resume';
    const parts = fileUrl.split('/');
    const fullName = parts[parts.length - 1];
    return fullName.replace(/_\d{13}(\.[^.]+)$/, '$1');
  }

  openResume(fileUrl: string) {
    if (!fileUrl) return;
    const filename = fileUrl.split('/').pop();
    const url = `http://localhost:8080/api/resumes/download/${filename}`;
    const token = localStorage.getItem('token');
    fetch(url, { headers: { Authorization: `Bearer ${token}` } })
      .then(res => res.blob())
      .then(blob => {
        const blobUrl = window.URL.createObjectURL(blob);
        window.open(blobUrl, '_blank');
      })
      .catch(err => console.error('Failed to open resume:', err));
  }
}
