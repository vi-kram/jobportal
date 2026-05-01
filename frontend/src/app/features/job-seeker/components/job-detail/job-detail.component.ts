import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { NavbarComponent } from '../../../../shared/components/navbar/navbar.component';
import { FooterComponent } from '../../../../shared/components/footer/footer.component';
import { JobService } from '../../services/job.service';
import { ApplicationService } from '../../services/application.service';
import { LucideAngularModule, MapPin, DollarSign, Briefcase, Bookmark, Share2, ArrowLeft, TrendingUp } from 'lucide-angular';

@Component({
  selector: 'app-job-detail',
  standalone: true,
  imports: [CommonModule, NavbarComponent, FooterComponent, RouterLink, LucideAngularModule],
  template: `
<div class="min-h-screen flex flex-col bg-gray-50">
  <app-navbar />
  <div *ngIf="loading" class="flex-1 flex items-center justify-center py-20">
    <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-700"></div>
  </div>
  <div *ngIf="!loading && error" class="flex-1 flex items-center justify-center text-red-500 py-20">{{ error }}</div>
  <div *ngIf="!loading && !error && !job" class="flex-1 flex items-center justify-center text-gray-500 py-20">Job not found</div>
  <main *ngIf="!loading && !error && job" class="flex-1 max-w-6xl mx-auto w-full px-6 py-8">
    <button (click)="goBack()" class="flex items-center gap-1.5 text-sm text-gray-500 hover:text-blue-600 mb-6">
      <lucide-icon [img]="ArrowLeft" [size]="15"></lucide-icon> Back to Jobs
    </button>
    <div class="flex gap-6">
      <div class="flex-1">
        <div class="bg-white rounded-xl border border-gray-100 p-6 mb-5">
          <div class="flex items-start justify-between">
            <div class="flex gap-4">
              <div class="w-16 h-16 bg-blue-50 rounded-xl flex items-center justify-center text-blue-600 font-bold text-2xl shrink-0">{{ job.company?.charAt(0) || 'C' }}</div>
              <div>
                <div class="flex items-center gap-3 mb-1">
                  <h1 class="text-xl font-bold text-gray-900">{{ job.title }}</h1>
                  <span [ngClass]="{'bg-green-100 text-green-700': job.status === 'OPEN','bg-red-100 text-red-600': job.status === 'CLOSED'}" class="text-xs font-semibold px-2.5 py-1 rounded-full">{{ job.status }}</span>
                </div>
                <p class="text-blue-600 font-medium mb-2">{{ job.company }}</p>
                <div class="flex flex-wrap gap-4 text-sm text-gray-500">
                  <span class="flex items-center gap-1"><lucide-icon [img]="MapPin" [size]="13"></lucide-icon> {{ job.location }}</span>
                  <span *ngIf="job.salary" class="flex items-center gap-1"><lucide-icon [img]="DollarSign" [size]="13"></lucide-icon> {{ job.salary | number }}/yr</span>
                  <span class="flex items-center gap-1"><lucide-icon [img]="Briefcase" [size]="13"></lucide-icon> {{ job.jobType || 'Full-time' }}</span>
                </div>
              </div>
            </div>
            <div class="flex flex-col gap-2 shrink-0">
              <button *ngIf="job.status === 'OPEN' && !applied" (click)="applyNow()" [disabled]="applying" class="bg-blue-700 hover:bg-blue-800 text-white font-semibold px-6 py-2.5 rounded-full transition disabled:opacity-50">{{ applying ? 'Applying...' : 'Apply Now' }}</button>
              <button *ngIf="applied" class="bg-green-100 text-green-700 font-semibold px-6 py-2.5 rounded-full cursor-default">Applied ✓</button>
              <div class="flex gap-2">
                <button class="border border-gray-200 text-gray-500 px-4 py-2 rounded-full text-sm hover:bg-gray-50 flex items-center gap-1.5"><lucide-icon [img]="Bookmark" [size]="13"></lucide-icon> Save</button>
                <button class="border border-gray-200 text-gray-500 px-4 py-2 rounded-full text-sm hover:bg-gray-50 flex items-center gap-1.5"><lucide-icon [img]="Share2" [size]="13"></lucide-icon> Share</button>
              </div>
            </div>
          </div>
        </div>
        <div class="bg-white rounded-xl border border-gray-100 p-6 mb-5">
          <h2 class="text-lg font-bold text-gray-900 mb-4">About the Role</h2>
          <p class="text-sm text-gray-600 leading-relaxed whitespace-pre-line">{{ job.description }}</p>
          <div *ngIf="getSkills().length > 0" class="mt-5">
            <h3 class="text-sm font-semibold text-gray-800 mb-2">Required Skills</h3>
            <div class="flex flex-wrap gap-2">
              <span *ngFor="let skill of getSkills()" class="bg-blue-50 text-blue-700 text-sm px-3 py-1 rounded-full font-medium">{{ skill }}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="w-72 shrink-0 space-y-4">
        <div class="bg-white rounded-xl border border-gray-100 p-5">
          <h3 class="font-bold text-gray-900 mb-4">About the Company</h3>
          <div class="flex items-center gap-3 mb-3">
            <div class="w-10 h-10 bg-blue-50 rounded-lg flex items-center justify-center text-blue-600 font-bold">{{ job.company?.charAt(0) }}</div>
            <div>
              <p class="font-semibold text-sm text-gray-800">{{ job.company }}</p>
              <a href="#" class="text-xs text-blue-600 hover:underline">View company profile</a>
            </div>
          </div>
            <div class="space-y-2 text-xs text-gray-500">
              <div class="flex justify-between"><span class="flex items-center gap-1"><lucide-icon [img]="MapPin" [size]="12"></lucide-icon> Location</span><span class="text-gray-700 font-medium">{{ job.location }}</span></div>
              <div class="flex justify-between"><span class="flex items-center gap-1"><lucide-icon [img]="Briefcase" [size]="12"></lucide-icon> Job Type</span><span class="text-gray-700 font-medium">{{ job.jobType || 'Full-time' }}</span></div>
              <div *ngIf="job.experienceLevel" class="flex justify-between"><span class="flex items-center gap-1"><lucide-icon [img]="TrendingUp" [size]="12"></lucide-icon> Experience</span><span class="text-gray-700 font-medium">{{ job.experienceLevel }}</span></div>
              <div *ngIf="job.salary" class="flex justify-between"><span class="flex items-center gap-1"><lucide-icon [img]="DollarSign" [size]="12"></lucide-icon> Salary</span><span class="text-gray-700 font-medium">\${{ job.salary | number }}/yr</span></div>
            </div>
        </div>
        <div class="bg-white rounded-xl border border-gray-100 p-5" *ngIf="similarJobs.length > 0">
          <div class="flex items-center justify-between mb-4">
            <h3 class="font-bold text-gray-900">Similar Jobs</h3>
            <a routerLink="/jobs/search" class="text-xs text-blue-600 hover:underline">View all</a>
          </div>
          <div class="space-y-3">
            <div *ngFor="let sj of similarJobs" (click)="viewSimilarJob(sj.jobId)" class="flex gap-3 cursor-pointer hover:bg-gray-50 rounded-lg p-2 transition">
              <div class="w-8 h-8 bg-blue-50 rounded-lg flex items-center justify-center text-blue-600 font-bold text-sm shrink-0">{{ sj.company?.charAt(0) }}</div>
              <div>
                <p class="text-sm font-medium text-gray-800">{{ sj.title }}</p>
                <p class="text-xs text-blue-600">{{ sj.company }}</p>
                <p class="text-xs text-gray-400 flex items-center gap-1"><lucide-icon [img]="MapPin" [size]="11"></lucide-icon> {{ sj.location }}</p>
              </div>
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
export class JobDetailComponent implements OnInit {

  readonly MapPin = MapPin;
  readonly DollarSign = DollarSign;
  readonly Briefcase = Briefcase;
  readonly Bookmark = Bookmark;
  readonly Share2 = Share2;
  readonly ArrowLeft = ArrowLeft;
  readonly TrendingUp = TrendingUp;

  job: any = null;
  similarJobs: any[] = [];
  loading = true;
  error = '';
  applying = false;
  applied = false;
  returnFilter = 'All Jobs';
  returnFrom = 'jobs';
  returnPage = 0;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private jobService: JobService,
    private applicationService: ApplicationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.route.queryParams.subscribe(qp => {
      this.returnFilter = qp['filter'] || 'All Jobs';
      this.returnFrom = qp['from'] || 'jobs';
      this.returnPage = +qp['page'] || 0;
    });
    this.route.params.subscribe(params => {
      const id = Number(params['id']);
      if (id) {
        this.loadJob(id);
        this.checkIfApplied(id);
      } else {
        this.error = 'Invalid job ID';
        this.loading = false;
      }
    });
  }

  checkIfApplied(jobId: number) {
    this.applicationService.getMyApplications(0, 100).subscribe({
      next: (res) => {
        const apps = res.content || res;
        this.applied = apps.some((a: any) => a.jobId === jobId);
        this.cdr.detectChanges();
      }
    });
  }

  loadJob(id: number) {
    this.loading = true;
    this.error = '';
    this.jobService.getJobById(id).subscribe({
      next: (res) => {
        this.job = res || null;
        this.loading = false;
        this.cdr.detectChanges();
        if (this.job) this.loadSimilarJobs();
      },
      error: (err) => {
        console.error('Job load error:', err);
        this.error = err.status === 404 ? 'Job not found' : 'Failed to load job. Please try again.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  loadSimilarJobs() {
    this.jobService.searchJobs({ keyword: this.job?.title?.split(' ')[0] }, 0, 3).subscribe({
      next: (res) => {
        this.similarJobs = (res.content || res).filter((j: any) => j.jobId !== this.job.jobId).slice(0, 3);
      }
    });
  }

  applyNow() {
    if (!this.job) return;
    this.applying = true;
    this.applicationService.applyForJob(this.job.jobId).subscribe({
      next: () => {
        this.applied = true;
        this.applying = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.applying = false;
        this.cdr.detectChanges();
      }
    });
  }

  goBack() {
    if (this.returnFrom === 'search') {
      this.router.navigate(['/jobs/search'], { queryParams: { page: this.returnPage } });
    } else if (this.returnFrom === 'applications') {
      this.router.navigate(['/applications'], { queryParams: { page: this.returnPage } });
    } else {
      this.router.navigate(['/jobs'], { queryParams: { filter: this.returnFilter, page: this.returnPage } });
    }
  }

  viewSimilarJob(jobId: number) {
    this.router.navigate(['/jobs', jobId]);
  }

  getSkills(): string[] {
    return this.job?.skills?.split(',').map((s: string) => s.trim()).filter((s: string) => s) || [];
  }
}
