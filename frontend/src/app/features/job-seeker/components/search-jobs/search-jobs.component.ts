import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { NavbarComponent } from '../../../../shared/components/navbar/navbar.component';
import { FooterComponent } from '../../../../shared/components/footer/footer.component';
import { JobService } from '../../services/job.service';
import { ApplicationService } from '../../services/application.service';

@Component({
  selector: 'app-search-jobs',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent, FooterComponent, RouterLink],
  template: `
<div class="min-h-screen flex flex-col" style="background:var(--bg)">
  <app-navbar />
  <main class="flex-1 flex flex-col md:flex-row">

    <!-- Sidebar Filters -->
    <aside class="w-full md:w-72 p-6 shrink-0" style="background:var(--surface); border-right:1px solid var(--border)">
      <div class="flex items-center justify-between mb-6">
        <h2 class="font-bold text-base" style="color:var(--text)">Filters</h2>
        <button (click)="clearFilters()" class="text-xs font-semibold hover:underline" style="color:var(--accent)">Clear All</button>
      </div>

      <div class="mb-6">
        <label class="block text-xs font-bold uppercase tracking-widest mb-3" style="color:var(--text-muted)">Location</label>
        <div class="flex items-center rounded-xl px-3 py-2.5 gap-2 mb-3" style="border:1.5px solid var(--border); background:var(--bg)">
          <span style="color:var(--accent)">📍</span>
          <input [(ngModel)]="location" type="text" placeholder="City, state, or zip"
            class="text-sm outline-none w-full bg-transparent" style="color:var(--text)" />
        </div>
        <label class="flex items-center gap-2 text-sm cursor-pointer" style="color:var(--text)">
          <input type="checkbox" [(ngModel)]="remoteOnly" (ngModelChange)="search()" style="accent-color:var(--accent)" />
          Remote Only
        </label>
      </div>

      <div class="mb-6">
        <label class="block text-xs font-bold uppercase tracking-widest mb-3" style="color:var(--text-muted)">Experience</label>
        <div class="flex flex-wrap gap-2">
          <button *ngFor="let level of experienceLevels" (click)="toggleExperience(level)"
            [style]="selectedExperience.includes(level) ? 'background:var(--accent); color:white' : 'background:var(--bg); color:var(--text); border:1.5px solid var(--border)'"
            class="text-xs px-3 py-1.5 rounded-full font-medium transition">
            {{ level }}
          </button>
        </div>
      </div>

      <div class="mb-6">
        <label class="block text-xs font-bold uppercase tracking-widest mb-3" style="color:var(--text-muted)">Job Type</label>
        <div class="flex flex-wrap gap-2">
          <button *ngFor="let type of jobTypes" (click)="toggleJobType(type)"
            [style]="selectedJobTypes.includes(type) ? 'background:var(--accent); color:white' : 'background:var(--bg); color:var(--text); border:1.5px solid var(--border)'"
            class="text-xs px-3 py-1.5 rounded-full font-medium transition">
            {{ type }}
          </button>
        </div>
      </div>

      <div class="mb-6">
        <label class="block text-xs font-bold uppercase tracking-widest mb-3" style="color:var(--text-muted)">Date Posted</label>
        <select [(ngModel)]="datePosted" class="w-full rounded-xl px-3 py-2.5 text-sm focus:outline-none"
          style="border:1.5px solid var(--border); background:var(--bg); color:var(--text)">
          <option *ngFor="let d of dateOptions">{{ d }}</option>
        </select>
      </div>

      <button (click)="search()" class="btn-primary w-full justify-center">Apply Filters</button>
    </aside>

    <!-- Main Content -->
    <div class="flex-1 p-4 md:p-6">

      <!-- Search Bar -->
      <div class="rounded-2xl p-4 mb-6" style="background:var(--surface); border:1px solid var(--border)">
        <div class="flex gap-2 mb-4">
          <button (click)="searchMode = 'keyword'"
            [style]="searchMode === 'keyword' ? 'background:var(--accent); color:white' : 'background:var(--bg); color:var(--text); border:1.5px solid var(--border)'"
            class="px-4 py-1.5 rounded-full text-sm font-semibold transition">
            Keyword
          </button>
          <button (click)="searchMode = 'skills'"
            [style]="searchMode === 'skills' ? 'background:var(--accent); color:white' : 'background:var(--bg); color:var(--text); border:1.5px solid var(--border)'"
            class="px-4 py-1.5 rounded-full text-sm font-semibold transition">
            Skills
          </button>
        </div>
        <div class="flex gap-3">
          <div class="flex-1 flex items-center rounded-xl px-4 py-3 gap-2" style="border:1.5px solid var(--border); background:var(--bg)">
            <span style="color:var(--accent)">🔍</span>
            <input [(ngModel)]="keyword" type="text"
              [placeholder]="searchMode === 'keyword' ? 'Job title, keywords...' : 'Skills e.g. React, Java...'"
              class="text-sm outline-none w-full bg-transparent" style="color:var(--text)" (keyup.enter)="search()" />
          </div>
          <button (click)="search()" class="btn-primary px-6">Search →</button>
        </div>
      </div>

      <!-- Results Header -->
      <div class="flex items-center justify-between mb-4" *ngIf="!loading">
        <p class="text-sm" style="color:var(--text-muted)">
          <span class="font-bold text-base" style="color:var(--accent)">{{ totalElements }}</span>
          <span class="ml-1">jobs{{ keyword ? ' for "' + keyword + '"' : ' found' }}</span>
        </p>
        <div class="flex items-center gap-2 text-sm" style="color:var(--text-muted)">
          Sort:
          <select [(ngModel)]="sortBy" (ngModelChange)="onSortChange($event)" class="rounded-lg px-2 py-1 text-sm focus:outline-none"
            style="border:1px solid var(--border); background:var(--bg); color:var(--text)">
            <option>Relevance</option><option>Newest</option><option>Salary</option>
          </select>
        </div>
      </div>

      <!-- Loading -->
      <div *ngIf="loading" class="space-y-3">
        <div *ngFor="let i of [1,2,3]" class="rounded-2xl p-5 animate-pulse" style="background:var(--surface)">
          <div class="flex gap-4">
            <div class="w-12 h-12 rounded-xl shrink-0" style="background:var(--border)"></div>
            <div class="flex-1">
              <div class="w-3/4 h-4 rounded mb-2" style="background:var(--border)"></div>
              <div class="w-1/2 h-3 rounded mb-2" style="background:var(--border)"></div>
              <div class="w-2/3 h-3 rounded" style="background:var(--border)"></div>
            </div>
          </div>
        </div>
      </div>

      <div *ngIf="error" class="text-center py-10" style="color:var(--danger)">{{ error }}</div>

      <!-- Job Cards -->
      <div *ngIf="!loading && !error" class="space-y-3">
        <div *ngFor="let job of displayedJobs"
          class="job-card rounded-2xl p-5 cursor-pointer"
          style="background:var(--surface); border:1px solid var(--border)"
          (click)="viewJob(job.jobId)">
          <div class="flex items-start gap-4">

            <!-- Avatar -->
            <div class="w-12 h-12 rounded-xl flex items-center justify-center text-white font-bold text-lg shrink-0 avatar-gradient">
              {{ job.company?.charAt(0) || 'C' }}
            </div>

            <div class="flex-1 min-w-0">
              <div class="flex items-start justify-between gap-2 mb-1">
                <h3 class="font-bold text-base leading-tight" style="color:var(--text)">{{ job.title }}</h3>
                <span class="badge shrink-0" [style]="job.status === 'OPEN' ? 'background:var(--accent-dim); color:var(--accent)' : 'background:var(--danger-dim); color:var(--danger)'">
                  {{ job.status }}
                </span>
              </div>
              <p class="text-sm font-semibold mb-1" style="color:var(--accent)">{{ job.company }}</p>
              <div class="flex flex-wrap gap-3 text-xs mb-2" style="color:var(--text-muted)">
                <span>📍 {{ job.location }}</span>
                <span *ngIf="job.salary">💰 \${{ job.salary | number }}/yr</span>
              </div>
              <p class="text-xs line-clamp-1 mb-2" style="color:var(--text-muted)">{{ job.description }}</p>
              <div *ngIf="job.skills" class="flex flex-wrap gap-1">
                <span *ngFor="let skill of job.skills?.split(',')?.slice(0,4)" class="skill-tag">{{ skill.trim() }}</span>
              </div>
            </div>

            <!-- Actions -->
            <div class="flex flex-col gap-2 shrink-0" (click)="$event.stopPropagation()">
              <button *ngIf="job.status === 'OPEN' && !appliedJobIds.has(job.jobId)"
                (click)="applyNow(job.jobId, $event)" [disabled]="applyingJobId === job.jobId"
                class="btn-primary text-xs px-4 py-2 disabled:opacity-50">
                {{ applyingJobId === job.jobId ? 'Applying...' : 'Apply →' }}
              </button>
              <span *ngIf="appliedJobIds.has(job.jobId)" class="badge" style="background:var(--accent-dim); color:var(--accent)">
                Applied ✓
              </span>
              <button class="btn-secondary text-xs px-4 py-2">🔖</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty -->
      <div *ngIf="!loading && !error && displayedJobs.length === 0" class="text-center py-20">
        <div class="text-6xl mb-4">🔍</div>
        <p class="text-xl font-bold" style="color:var(--text)">No jobs found</p>
        <p class="text-sm mt-2" style="color:var(--text-muted)">Try different keywords or adjust your filters</p>
      </div>

      <!-- Pagination -->
      <div *ngIf="totalPages > 1" class="flex items-center justify-center gap-2 mt-6">
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
export class SearchJobsComponent implements OnInit {

  keyword = '';
  location = '';
  remoteOnly = false;
  selectedJobTypes: string[] = [];
  selectedExperience: string[] = [];
  datePosted = 'Past Week';
  searchMode = 'keyword';
  sortBy = 'Relevance';

  allJobs: any[] = [];
  displayedJobs: any[] = [];
  loading = false;
  error = '';
  totalElements = 0;
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;

  experienceLevels = ['Internship', 'Entry level', 'Associate', 'Mid-Senior level', 'Director'];
  jobTypes = ['Full-time', 'Part-time', 'Contract', 'Remote'];
  dateOptions = ['Any Time', 'Past 24 hours', 'Past Week', 'Past Month'];

  appliedJobIds = new Set<number>();
  applyingJobId: number | null = null;

  constructor(
    private jobService: JobService,
    private applicationService: ApplicationService,
    private router: Router,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadAppliedJobs();
    const page = +(this.route.snapshot.queryParamMap.get('page') || 0);
    const q = this.route.snapshot.queryParamMap.get('q') || '';
    this.currentPage = page;
    if (q) this.keyword = q;
    this.search(false);
  }

  loadAppliedJobs() {
    this.applicationService.getMyApplications(0, 100).subscribe({
      next: (res) => {
        const apps = res.content || res;
        this.appliedJobIds = new Set(apps.map((a: any) => a.jobId));
        this.cdr.detectChanges();
      }
    });
  }

  search(resetPage = true) {
    this.loading = true;
    this.error = '';
    if (resetPage) this.currentPage = 0;
    const filters = { keyword: this.keyword, location: this.remoteOnly ? '' : this.location };
    this.jobService.searchJobs(filters, 0, 500).subscribe({
      next: (res) => {
        let jobs = res.content || res;
        if (this.remoteOnly) jobs = jobs.filter((j: any) => j.jobType?.toLowerCase() === 'remote');
        if (this.selectedJobTypes.length > 0) {
          jobs = jobs.filter((j: any) => {
            if (this.selectedJobTypes.includes('Full-time') && (!j.jobType || j.jobType.toLowerCase() === 'full-time')) return true;
            return this.selectedJobTypes.some(t => j.jobType?.toLowerCase() === t.toLowerCase());
          });
        }
        if (this.selectedExperience.length > 0) {
          jobs = jobs.filter((j: any) =>
            this.selectedExperience.some(e => j.experienceLevel?.toLowerCase() === e.toLowerCase())
          );
        }
        this.allJobs = jobs;
        this.totalElements = jobs.length;
        this.loading = false;
        this.applySortAndPaginate();
      },
      error: () => { this.error = 'Failed to search jobs'; this.loading = false; this.cdr.detectChanges(); }
    });
  }

  onSortChange(sort: string) { this.sortBy = sort; this.currentPage = 0; this.applySortAndPaginate(); }

  applySortAndPaginate() {
    let sorted = [...this.allJobs];
    if (this.sortBy === 'Newest') sorted = sorted.sort((a: any, b: any) => (b.jobId || 0) - (a.jobId || 0));
    else if (this.sortBy === 'Salary') sorted = sorted.sort((a: any, b: any) => (b.salary || 0) - (a.salary || 0));
    this.totalPages = Math.ceil(sorted.length / this.pageSize);
    const start = this.currentPage * this.pageSize;
    this.displayedJobs = sorted.slice(start, start + this.pageSize);
    this.cdr.detectChanges();
  }

  toggleJobType(type: string) {
    const idx = this.selectedJobTypes.indexOf(type);
    if (idx > -1) this.selectedJobTypes.splice(idx, 1); else this.selectedJobTypes.push(type);
    this.search();
  }

  toggleExperience(level: string) {
    const idx = this.selectedExperience.indexOf(level);
    if (idx > -1) this.selectedExperience.splice(idx, 1); else this.selectedExperience.push(level);
    this.search();
  }

  clearFilters() {
    this.keyword = ''; this.location = ''; this.remoteOnly = false;
    this.selectedJobTypes = []; this.selectedExperience = [];
    this.sortBy = 'Relevance';
    this.search();
  }

  goToPage(page: number) {
    if (page < 0 || page >= this.totalPages) return;
    this.currentPage = page;
    this.applySortAndPaginate();
    window.scrollTo(0, 0);
  }

  viewJob(jobId: number) { this.router.navigate(['/jobs', jobId], { queryParams: { from: 'search', page: this.currentPage } }); }

  applyNow(jobId: number, event: Event) {
    event.stopPropagation();
    this.applyingJobId = jobId;
    this.applicationService.applyForJob(jobId).subscribe({
      next: () => { this.appliedJobIds.add(jobId); this.applyingJobId = null; this.cdr.detectChanges(); },
      error: () => { this.applyingJobId = null; this.cdr.detectChanges(); }
    });
  }

  getPages(): number[] {
    const start = Math.max(0, this.currentPage - 2);
    const end = Math.min(this.totalPages, start + 5);
    return Array.from({ length: end - start }, (_, i) => start + i);
  }
}
