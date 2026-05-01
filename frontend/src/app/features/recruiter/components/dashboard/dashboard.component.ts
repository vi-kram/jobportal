import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { NavbarComponent } from '../../../../shared/components/navbar/navbar.component';
import { FooterComponent } from '../../../../shared/components/footer/footer.component';
import { RecruiterJobService } from '../../services/recruiter-job.service';
import { ApplicantService } from '../../services/applicant.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, NavbarComponent, FooterComponent],
  template: `
<div class="min-h-screen flex flex-col" style="background:var(--bg)">
  <app-navbar />
  <main class="flex-1 max-w-6xl mx-auto w-full px-6 py-8">

    <!-- Header -->
    <div class="flex items-center justify-between mb-6">
      <div>
        <p class="section-label mb-2">Recruiter</p>
        <h1 class="text-2xl font-bold" style="color:var(--text)">Dashboard</h1>
        <p class="text-sm mt-1" style="color:var(--text-muted)">Monitor your job postings, applicant pipeline, and key metrics.</p>
      </div>
      <div class="flex gap-3">
        <button (click)="exportReport()" class="btn-secondary text-sm px-4 py-2">Export Report</button>
        <button (click)="postJob()" class="btn-primary text-sm px-4 py-2">+ Post a Job</button>
      </div>
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <div class="job-card rounded-xl p-5" style="background:var(--surface); border:1px solid var(--border)">
        <div class="flex items-center justify-between mb-2">
          <p class="text-xs font-bold uppercase tracking-widest" style="color:var(--text-muted)">Open Roles</p>
          <span class="text-xl">💼</span>
        </div>
        <p *ngIf="!loading" class="text-3xl font-bold" style="color:var(--text)">{{ openRoles }}</p>
        <div *ngIf="loading" class="w-16 h-8 rounded animate-pulse mt-1" style="background:var(--border)"></div>
        <p class="text-xs mt-2" style="color:var(--accent)">↑ Active listings</p>
      </div>
      <div class="job-card rounded-xl p-5" style="background:var(--surface); border:1px solid var(--border)">
        <div class="flex items-center justify-between mb-2">
          <p class="text-xs font-bold uppercase tracking-widest" style="color:var(--text-muted)">Applicants</p>
          <span class="text-xl">👥</span>
        </div>
        <p *ngIf="!loading" class="text-3xl font-bold" style="color:var(--text)">{{ totalApplicants }}</p>
        <div *ngIf="loading" class="w-16 h-8 rounded animate-pulse mt-1" style="background:var(--border)"></div>
        <p class="text-xs mt-2" style="color:var(--accent)">↑ Total received</p>
      </div>
      <div class="job-card rounded-xl p-5" style="background:var(--surface); border:1px solid var(--border)">
        <div class="flex items-center justify-between mb-2">
          <p class="text-xs font-bold uppercase tracking-widest" style="color:var(--text-muted)">Shortlisted</p>
          <span class="text-xl">⭐</span>
        </div>
        <p *ngIf="!loading" class="text-3xl font-bold" style="color:var(--text)">{{ shortlisted }}</p>
        <div *ngIf="loading" class="w-16 h-8 rounded animate-pulse mt-1" style="background:var(--border)"></div>
        <span class="badge mt-2" style="background:var(--warn-dim); color:var(--warn-text)">Needs Review</span>
      </div>
      <div class="job-card rounded-xl p-5" style="background:var(--surface); border:1px solid var(--border)">
        <div class="flex items-center justify-between mb-2">
          <p class="text-xs font-bold uppercase tracking-widest" style="color:var(--text-muted)">Interviews</p>
          <span class="text-xl">📅</span>
        </div>
        <p *ngIf="!loading" class="text-3xl font-bold" style="color:var(--text)">{{ interviews }}</p>
        <div *ngIf="loading" class="w-16 h-8 rounded animate-pulse mt-1" style="background:var(--border)"></div>
        <span class="badge mt-2" style="background:var(--accent-dim); color:var(--accent)">This Week</span>
      </div>
    </div>

    <!-- Recent Jobs -->
    <div class="rounded-xl p-6 mb-6" style="background:var(--surface); border:1px solid var(--border)">
      <div class="flex items-center justify-between mb-4">
        <h2 class="font-bold" style="color:var(--text)">Recent Job Postings</h2>
        <button (click)="viewMyJobs()" class="text-sm font-semibold hover:underline" style="color:var(--accent)">View All →</button>
      </div>

      <div *ngIf="loading" class="animate-pulse space-y-3">
        <div *ngFor="let i of [1,2,3]" class="flex gap-4 py-3" style="border-bottom:1px solid var(--border)">
          <div class="flex-1 h-3 rounded" style="background:var(--border)"></div>
          <div class="w-16 h-5 rounded-full" style="background:var(--border)"></div>
          <div class="flex-1 h-3 rounded" style="background:var(--border)"></div>
        </div>
      </div>

      <div *ngIf="!loading && jobs.length === 0" class="text-center py-8" style="color:var(--text-muted)">
        <p>No jobs posted yet.</p>
        <button (click)="postJob()" class="mt-3 text-sm font-semibold hover:underline" style="color:var(--accent)">Post your first job →</button>
      </div>

      <div *ngIf="!loading && jobs.length > 0" class="overflow-x-auto">
        <table class="w-full">
          <thead style="background:var(--surface-alt)">
            <tr>
              <th class="text-left text-xs font-bold uppercase tracking-widest px-4 py-3" style="color:var(--text-muted)">Job Title</th>
              <th class="text-left text-xs font-bold uppercase tracking-widest px-4 py-3" style="color:var(--text-muted)">Status</th>
              <th class="text-left text-xs font-bold uppercase tracking-widest px-4 py-3" style="color:var(--text-muted)">Location</th>
              <th class="text-left text-xs font-bold uppercase tracking-widest px-4 py-3" style="color:var(--text-muted)">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let job of jobs.slice(0,5)" style="border-bottom:1px solid var(--border)"
              onmouseover="this.style.background='var(--bg)'" onmouseout="this.style.background='transparent'">
              <td class="px-4 py-3">
                <p class="text-sm font-semibold" style="color:var(--text)">{{ job.title }}</p>
                <p class="text-xs" style="color:var(--text-muted)">{{ job.company }}</p>
              </td>
              <td class="px-4 py-3">
                <span class="badge" [style]="job.status === 'OPEN' ? 'background:var(--accent-dim); color:var(--accent)' : 'background:var(--danger-dim); color:var(--danger)'">{{ job.status }}</span>
              </td>
              <td class="px-4 py-3 text-sm" style="color:var(--text-muted)">{{ job.location }}</td>
              <td class="px-4 py-3">
                <button (click)="viewApplicants(job.jobId)" class="text-sm font-semibold hover:underline" style="color:var(--accent)">View Applicants →</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Quick Actions -->
    <div class="rounded-xl p-6" style="background:var(--surface); border:1px solid var(--border)">
      <h2 class="font-bold mb-4" style="color:var(--text)">Quick Actions</h2>
      <div class="flex gap-3">
        <button (click)="postJob()" class="btn-primary flex-1 justify-center py-3">+ Post a New Job</button>
        <button (click)="viewMyJobs()" class="btn-secondary flex-1 justify-center py-3">📋 My Jobs</button>
        <button (click)="viewApplicants(0)" class="btn-secondary flex-1 justify-center py-3">👥 All Applicants</button>
      </div>
    </div>

  </main>
  <app-footer />
</div>
  `
})
export class DashboardComponent implements OnInit {

  jobs: any[] = [];
  loading = true;
  openRoles = 0;
  totalApplicants = 0;
  shortlisted = 0;
  interviews = 0;

  constructor(
    private recruiterJobService: RecruiterJobService,
    private applicantService: ApplicantService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() { this.loadJobs(); }

  loadJobs() {
    this.loading = true;
    this.recruiterJobService.getMyJobs(0, 100).subscribe({
      next: (res) => {
        this.jobs = (res.content || res).slice(0, 5);
        const allJobs = res.content || res;
        this.openRoles = allJobs.filter((j: any) => j.status === 'OPEN').length;
        this.loading = false;
        this.loadApplicantStats(allJobs);
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.warn('Dashboard jobs error:', err.status);
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  loadApplicantStats(allJobs: any[]) {
    this.totalApplicants = 0;
    this.shortlisted = 0;
    this.interviews = 0;
    allJobs.forEach((job: any) => {
      this.applicantService.getApplicants(job.jobId, 0, 100).subscribe({
        next: (res) => {
          const apps = res.content || res;
          this.totalApplicants += res.totalElements || apps.length;
          this.shortlisted += apps.filter((a: any) => a.status === 'SHORTLISTED').length;
          this.interviews += apps.filter((a: any) => a.status === 'INTERVIEW_SCHEDULED').length;
          this.cdr.detectChanges();
        }
      });
    });
  }

  exportReport() {
    const rows = [['Job Title', 'Company', 'Location', 'Status']]
      .concat(this.jobs.map((j: any) => [j.title, j.company, j.location, j.status]));
    const csv = rows.map(r => r.join(',')).join('\n');
    const a = document.createElement('a');
    a.href = 'data:text/csv;charset=utf-8,' + encodeURIComponent(csv);
    a.download = 'dashboard-report.csv';
    a.click();
  }

  postJob() { this.router.navigate(['/recruiter/post-job']); }
  viewMyJobs() { this.router.navigate(['/recruiter/my-jobs']); }
  viewApplicants(jobId: number) { this.router.navigate(['/recruiter/applicants', jobId]); }
}
