import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { NavbarComponent } from '../../../../shared/components/navbar/navbar.component';
import { FooterComponent } from '../../../../shared/components/footer/footer.component';
import { ApplicationService } from '../../services/application.service';
import { JobService } from '../../services/job.service';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../../../../environments/environment';

@Component({
  selector: 'app-my-applications',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent, FooterComponent],
  template: `
<div class="min-h-screen flex flex-col" style="background:var(--bg)">
  <app-navbar />
  <main class="flex-1 max-w-5xl mx-auto w-full px-4 md:px-6 py-8">

    <!-- Header -->
    <div class="flex flex-col sm:flex-row items-start justify-between mb-8 gap-4">
      <div>
        <p class="section-label mb-2">My Journey</p>
        <h1 class="text-3xl font-bold" style="color:var(--text)">Applications</h1>
        <p class="text-sm mt-1" style="color:var(--text-muted)">Track every step of your job search.</p>
      </div>
      <div class="flex gap-3">
        <div class="rounded-2xl px-5 py-4 text-center" style="background:var(--surface); border:1px solid var(--border)">
          <p class="text-3xl font-bold" style="color:var(--accent)">{{ totalElements }}</p>
          <p class="text-xs font-semibold uppercase tracking-widest mt-1" style="color:var(--text-muted)">Applied</p>
        </div>
        <div class="rounded-2xl px-5 py-4 text-center" style="background:var(--surface); border:1px solid var(--border)">
          <p class="text-3xl font-bold" style="color:var(--accent)">{{ totalInterviews }}</p>
          <p class="text-xs font-semibold uppercase tracking-widest mt-1" style="color:var(--text-muted)">Interviews</p>
        </div>
      </div>
    </div>

    <!-- Filters -->
    <div class="rounded-2xl p-4 mb-6 flex flex-col sm:flex-row gap-3" style="background:var(--surface); border:1px solid var(--border)">
      <div class="flex-1 flex items-center rounded-xl px-3 py-2.5 gap-2" style="border:1px solid var(--border); background:var(--bg)">
        <span style="color:var(--accent)">🔍</span>
        <input [(ngModel)]="searchTerm" (ngModelChange)="applyFilter()" type="text"
          placeholder="Search by job title or company..." class="text-sm outline-none w-full bg-transparent" style="color:var(--text)" />
      </div>
      <select [(ngModel)]="selectedStatus" (ngModelChange)="applyFilter()"
        class="rounded-xl px-3 py-2.5 text-sm focus:outline-none" style="border:1px solid var(--border); background:var(--bg); color:var(--text)">
        <option *ngFor="let s of statuses">{{ s }}</option>
      </select>
    </div>

    <!-- Loading -->
    <div *ngIf="loading" class="space-y-3">
      <div *ngFor="let i of [1,2,3]" class="rounded-2xl p-5 animate-pulse flex gap-4" style="background:var(--surface)">
        <div class="w-12 h-12 rounded-xl shrink-0" style="background:var(--border)"></div>
        <div class="flex-1">
          <div class="w-1/2 h-4 rounded mb-2" style="background:var(--border)"></div>
          <div class="w-1/3 h-3 rounded" style="background:var(--border)"></div>
        </div>
      </div>
    </div>
    <div *ngIf="error" class="text-center py-10" style="color:var(--danger)">{{ error }}</div>

    <!-- Timeline Cards -->
    <div *ngIf="!loading && !error" class="space-y-3">
      <div *ngFor="let app of displayedApplications; let i = index"
        class="job-card rounded-2xl p-5 flex flex-col sm:flex-row items-start sm:items-center gap-4"
        style="background:var(--surface); border:1px solid var(--border)">

        <!-- Step Number -->
        <div class="w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold shrink-0 avatar-gradient">
          {{ (currentPage * pageSize) + i + 1 }}
        </div>

        <!-- Job Info -->
        <div class="flex-1 min-w-0">
          <button (click)="viewJob(app.jobId)" class="text-base font-bold hover:underline text-left" style="color:var(--text)">
            {{ jobMap[app.jobId]?.title || 'Job #' + app.jobId }}
          </button>
          <p class="text-sm font-medium mt-0.5" style="color:var(--accent)">{{ jobMap[app.jobId]?.company || '—' }}</p>
          <p class="text-xs mt-1" style="color:var(--text-muted)">📅 Applied {{ app.appliedAt | date:'MMM dd, yyyy' }}</p>
        </div>

        <!-- Status Badge -->
        <span class="badge shrink-0" [style]="getStatusStyle(app.status)">
          {{ getStatusLabel(app.status) }}
        </span>

        <!-- Actions -->
        <div class="flex gap-2 shrink-0">
          <button (click)="viewJob(app.jobId)" class="btn-secondary text-xs px-3 py-1.5">View →</button>
          <button *ngIf="app.status === 'APPLIED'" (click)="withdraw(app.applicationId)"
            class="text-xs px-3 py-1.5 rounded-full transition" style="border:1px solid var(--danger); color:var(--danger)">Withdraw</button>
        </div>
      </div>
    </div>

    <!-- Empty -->
    <div *ngIf="!loading && !error && displayedApplications.length === 0" class="text-center py-20">
      <div class="text-6xl mb-4">📭</div>
      <p class="text-xl font-bold" style="color:var(--text)">No applications yet</p>
      <p class="text-sm mt-2 mb-6" style="color:var(--text-muted)">Start applying to jobs and track your progress here.</p>
      <a routerLink="/jobs/search" class="btn-primary">Browse Jobs →</a>
    </div>

    <!-- Pagination -->
    <div *ngIf="totalPages > 1" class="flex items-center justify-between mt-6">
      <p class="text-sm" style="color:var(--text-muted)">
        Showing <span class="font-semibold" style="color:var(--accent)">{{ displayedApplications.length }}</span> of
        <span class="font-semibold" style="color:var(--accent)">{{ totalElements }}</span>
      </p>
      <div class="flex gap-1">
        <button (click)="goToPage(currentPage - 1)" [disabled]="currentPage === 0"
          class="px-3 py-1.5 rounded-lg text-sm disabled:opacity-40" style="border:1px solid var(--border); color:var(--accent)">‹</button>
        <button *ngFor="let p of getPages()" (click)="goToPage(p)"
          [style]="currentPage === p ? 'background:var(--accent); color:white' : 'border:1px solid var(--border); color:var(--accent)'"
          class="px-3 py-1.5 rounded-lg text-sm font-medium">{{ p + 1 }}</button>
        <button (click)="goToPage(currentPage + 1)" [disabled]="currentPage >= totalPages - 1"
          class="px-3 py-1.5 rounded-lg text-sm disabled:opacity-40" style="border:1px solid var(--border); color:var(--accent)">›</button>
      </div>
    </div>

  </main>
  <app-footer />
</div>
  `
})
export class MyApplicationsComponent implements OnInit {

  allApplications: any[] = [];
  applications: any[] = [];
  displayedApplications: any[] = [];
  jobMap: { [jobId: number]: any } = {};
  base = environment.apiUrl;
  loading = true;
  error = '';
  searchTerm = '';
  selectedStatus = 'All Statuses';
  currentPage = 0;
  totalPages = 0;
  totalElements = 0;
  pageSize = 10;

  statuses = ['All Statuses', 'APPLIED', 'SHORTLISTED', 'INTERVIEW_SCHEDULED', 'REJECTED'];

  constructor(
    private applicationService: ApplicationService,
    private jobService: JobService,
    private router: Router,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef,
    private http: HttpClient
  ) {}

  ngOnInit() {
    this.currentPage = +(this.route.snapshot.queryParamMap.get('page') || 0);
    this.loadApplications();
  }

  loadApplications() {
    this.loading = true;
    // Fetch all applications for client-side sort + pagination
    this.applicationService.getMyApplications(0, 500).subscribe({
      next: (res) => {
        const raw = res.content || res;
        this.allApplications = raw.sort((a: any, b: any) =>
          new Date(b.appliedAt).getTime() - new Date(a.appliedAt).getTime()
        );
        this.applications = this.allApplications;
        this.totalElements = res.totalElements || this.allApplications.length;
        this.totalPages = Math.ceil(this.allApplications.length / this.pageSize);
        this.fetchJobDetails();
      },
      error: () => {
        this.error = 'Failed to load applications';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  fetchJobDetails() {
    const uniqueJobIds = [...new Set(this.applications.map(a => a.jobId))];
    if (uniqueJobIds.length === 0) {
      this.loading = false;
      this.applyFilter();
      return;
    }

    const requests = uniqueJobIds.map(id =>
      this.jobService.getJobById(id).pipe(
        catchError(() =>
          // fallback to job-service if search-service doesn't have it
          this.http.get(`${this.base}/api/jobs/${id}`).pipe(
            catchError(() => of({ jobId: id, title: 'Job #' + id, company: '—' }))
          )
        )
      )
    );

    forkJoin(requests).subscribe({
      next: (jobs) => {
        jobs.forEach((job: any) => {
          if (job) this.jobMap[job.jobId] = job;
        });
        this.loading = false;
        this.applyFilter();
      },
      error: () => {
        this.loading = false;
        this.applyFilter();
      }
    });
  }

  applyFilter() {
    const filtered = this.allApplications.filter(app => {
      const jobTitle = this.jobMap[app.jobId]?.title?.toLowerCase() || '';
      const matchSearch = !this.searchTerm ||
        jobTitle.includes(this.searchTerm.toLowerCase()) ||
        app.jobId?.toString().includes(this.searchTerm);
      const matchStatus = this.selectedStatus === 'All Statuses' || app.status === this.selectedStatus;
      return matchSearch && matchStatus;
    });
    this.totalElements = filtered.length;
    this.totalPages = Math.ceil(filtered.length / this.pageSize);
    const start = this.currentPage * this.pageSize;
    this.displayedApplications = filtered.slice(start, start + this.pageSize);
    this.cdr.detectChanges();
  }

  get totalInterviews() {
    return this.allApplications.filter(a => a.status === 'INTERVIEW_SCHEDULED').length;
  }

  getStatusClass(status: string): string {
    const map: any = {
      'APPLIED':              'text-xs font-semibold px-3 py-1 rounded-full" style="background:var(--accent-dim); color:var(--accent)',
      'SHORTLISTED':          'text-xs font-semibold px-3 py-1 rounded-full" style="background:var(--warn-dim); color:var(--warn-text)',
      'INTERVIEW_SCHEDULED':  'text-xs font-semibold px-3 py-1 rounded-full" style="background:#ede9fe; color:#5b21b6',
      'REJECTED':             'text-xs font-semibold px-3 py-1 rounded-full" style="background:var(--danger-dim); color:var(--danger)'
    };
    return map[status] ? '' : '';
  }

  getStatusStyle(status: string): string {
    const map: any = {
      'APPLIED':             'background:var(--accent-dim); color:var(--accent)',
      'SHORTLISTED':         'background:var(--warn-dim); color:var(--warn-text)',
      'INTERVIEW_SCHEDULED': 'background:#ede9fe; color:#5b21b6',
      'REJECTED':            'background:var(--danger-dim); color:var(--danger)'
    };
    return map[status] || 'background:var(--surface); color:var(--text)';
  }

  getStatusLabel(status: string): string {
    const map: any = {
      'APPLIED': 'Applied',
      'SHORTLISTED': 'Shortlisted',
      'INTERVIEW_SCHEDULED': 'Interview Scheduled',
      'REJECTED': 'Rejected'
    };
    return map[status] || status;
  }

  viewJob(jobId: number) { this.router.navigate(['/jobs', jobId], { queryParams: { from: 'applications', page: this.currentPage } }); }

  withdraw(applicationId: string) {
    if (!confirm('Are you sure you want to withdraw this application?')) return;
    this.applicationService.withdrawApplication(applicationId).subscribe({
      next: () => {
        this.allApplications = this.allApplications.filter(a => a.applicationId !== applicationId);
        this.applyFilter();
      },
      error: (err) => alert(err?.error?.message || 'Could not withdraw application')
    });
  }

  goToPage(page: number) {
    if (page < 0 || page >= this.totalPages) return;
    this.currentPage = page;
    this.applyFilter();
  }

  getPages(): number[] {
    return Array.from({ length: Math.min(this.totalPages, 5) }, (_, i) => i);
  }
}
