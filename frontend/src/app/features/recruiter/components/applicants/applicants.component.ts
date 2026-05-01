import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { NavbarComponent } from '../../../../shared/components/navbar/navbar.component';
import { FooterComponent } from '../../../../shared/components/footer/footer.component';
import { ApplicantService } from '../../services/applicant.service';
import { RecruiterJobService } from '../../services/recruiter-job.service';

@Component({
  selector: 'app-applicants',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent, FooterComponent],
  template: `
<div class="min-h-screen flex flex-col" style="background:var(--bg)">
  <app-navbar />
  <main class="flex-1 max-w-6xl mx-auto w-full px-6 py-8">

    <!-- Breadcrumb -->
    <div class="flex items-center gap-2 text-sm mb-4" style="color:var(--text-muted)">
      <button (click)="router.navigate(['/recruiter/my-jobs'])" class="hover:underline" style="color:var(--accent)">My Jobs</button>
      <span>›</span>
      <span class="font-semibold" style="color:var(--text)">Applicants</span>
    </div>

    <!-- Header -->
    <div class="flex items-start justify-between mb-6">
      <div>
        <p class="section-label mb-2">Recruiter</p>
        <h1 class="text-2xl font-bold" style="color:var(--text)">Applicants Management</h1>
        <p class="text-sm mt-1" style="color:var(--text-muted)">{{ jobId ? 'Review and manage candidates for Job #' + jobId : 'All applicants across your job postings' }}</p>
      </div>
      <div class="flex gap-3 items-start">
        <button (click)="exportCsv()" class="btn-secondary text-sm px-4 py-2">Export CSV</button>
        <div class="rounded-xl px-5 py-3 text-center" style="background:var(--surface); border:1px solid var(--border)">
          <p class="text-2xl font-bold" style="color:var(--accent)">{{ totalElements }}</p>
          <p class="text-xs font-bold uppercase tracking-widest mt-1" style="color:var(--text-muted)">Total</p>
        </div>
        <div class="rounded-xl px-5 py-3 text-center" style="background:var(--surface); border:1px solid var(--border)">
          <p class="text-2xl font-bold" style="color:var(--accent)">{{ shortlistedCount }}</p>
          <p class="text-xs font-bold uppercase tracking-widest mt-1" style="color:var(--text-muted)">Shortlisted</p>
        </div>
      </div>
    </div>

    <!-- Filters -->
    <div class="rounded-xl p-4 mb-5 flex gap-3" style="background:var(--surface); border:1px solid var(--border)">
      <div class="flex-1 flex items-center rounded-lg px-3 py-2 gap-2" style="border:1px solid var(--border); background:var(--bg)">
        <span style="color:var(--accent)">🔍</span>
        <input [(ngModel)]="searchTerm" type="text" placeholder="Search applicants..."
          class="text-sm outline-none w-full bg-transparent" style="color:var(--text)" />
      </div>
      <select [(ngModel)]="statusFilter" class="rounded-lg px-3 py-2 text-sm focus:outline-none"
        style="border:1px solid var(--border); background:var(--bg); color:var(--text)">
        <option value="">All Statuses</option>
        <option value="APPLIED">Applied</option>
        <option value="SHORTLISTED">Shortlisted</option>
        <option value="INTERVIEW_SCHEDULED">Interview Scheduled</option>
        <option value="REJECTED">Rejected</option>
      </select>
    </div>

    <!-- Loading -->
    <div *ngIf="loading" class="rounded-xl p-8 text-center" style="background:var(--surface)">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 mx-auto" style="border-color:var(--accent)"></div>
    </div>

    <!-- Table -->
    <div *ngIf="!loading" class="rounded-xl overflow-hidden" style="background:var(--surface); border:1px solid var(--border)">
      <table class="w-full">
        <thead style="background:var(--surface-alt)">
          <tr>
            <th class="text-left text-xs font-bold uppercase tracking-widest px-5 py-3" style="color:var(--text-muted)">Candidate</th>
            <th *ngIf="!jobId" class="text-left text-xs font-bold uppercase tracking-widest px-5 py-3" style="color:var(--text-muted)">Job</th>
            <th class="text-left text-xs font-bold uppercase tracking-widest px-5 py-3" style="color:var(--text-muted)">Applied Date</th>
            <th class="text-left text-xs font-bold uppercase tracking-widest px-5 py-3" style="color:var(--text-muted)">Status</th>
            <th class="text-left text-xs font-bold uppercase tracking-widest px-5 py-3" style="color:var(--text-muted)">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let app of filteredApplications" class="transition cursor-pointer"
            style="border-bottom:1px solid var(--border)"
            onmouseover="this.style.background='var(--bg)'" onmouseout="this.style.background='transparent'"
            (click)="openDetail(app)">
            <td class="px-5 py-4">
              <div class="flex items-center gap-3">
                <div class="w-9 h-9 rounded-full flex items-center justify-center font-bold text-sm avatar-gradient">
                  {{ app.userEmail?.charAt(0)?.toUpperCase() }}
                </div>
                <div>
                  <p class="text-sm font-semibold" style="color:var(--text)">{{ app.userEmail }}</p>
                  <p class="text-xs" style="color:var(--text-muted)">#{{ app.applicationId?.substring(0,8) }}</p>
                </div>
              </div>
            </td>
            <td *ngIf="!jobId" class="px-5 py-4">
              <p class="text-sm font-semibold" style="color:var(--text)">{{ app.jobTitle || 'Job #' + app.jobId }}</p>
              <p class="text-xs" style="color:var(--text-muted)">{{ app.jobCompany }}</p>
            </td>
            <td class="px-5 py-4 text-sm" style="color:var(--text-muted)">{{ app.appliedAt | date:'MMM dd, yyyy' }}</td>
            <td class="px-5 py-4">
              <span class="badge" [style]="getStatusStyle(app.status)">{{ getStatusLabel(app.status) }}</span>
            </td>
            <td class="px-5 py-4" (click)="$event.stopPropagation()">
              <select (change)="updateStatus(app.applicationId, $event)"
                class="rounded-lg px-2 py-1 text-xs focus:outline-none"
                style="border:1px solid var(--border); background:var(--bg); color:var(--text)">
                <option value="APPLIED" [selected]="app.status === 'APPLIED'">Applied</option>
                <option value="SHORTLISTED" [selected]="app.status === 'SHORTLISTED'">Shortlist</option>
                <option value="INTERVIEW_SCHEDULED" [selected]="app.status === 'INTERVIEW_SCHEDULED'">Interview</option>
                <option value="REJECTED" [selected]="app.status === 'REJECTED'">Reject</option>
              </select>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- Empty -->
      <div *ngIf="filteredApplications.length === 0" class="text-center py-12" style="color:var(--text-muted)">
        <div class="text-4xl mb-3">👥</div>
        <p class="font-semibold" style="color:var(--text)">No applicants yet</p>
        <p class="text-sm mt-1">Applicants will appear here once they apply</p>
      </div>

      <!-- Pagination -->
      <div *ngIf="totalPages > 1" class="flex items-center justify-between px-5 py-3" style="border-top:1px solid var(--border)">
        <p class="text-sm" style="color:var(--text-muted)">
          Showing <span class="font-semibold" style="color:var(--accent)">{{ filteredApplications.length }}</span> of
          <span class="font-semibold" style="color:var(--accent)">{{ totalElements }}</span>
        </p>
        <div class="flex gap-1">
          <button (click)="goToPage(currentPage - 1)" [disabled]="currentPage === 0"
            class="px-3 py-1.5 rounded-lg text-sm disabled:opacity-40"
            style="border:1px solid var(--border); color:var(--accent)">&#8249;</button>
          <button *ngFor="let p of getPages()" (click)="goToPage(p)"
            [style]="currentPage === p ? 'background:var(--accent); color:white' : 'border:1px solid var(--border); color:var(--accent)'"
            class="px-3 py-1.5 rounded-lg text-sm font-medium">{{ p + 1 }}</button>
          <button (click)="goToPage(currentPage + 1)" [disabled]="currentPage >= totalPages - 1"
            class="px-3 py-1.5 rounded-lg text-sm disabled:opacity-40"
            style="border:1px solid var(--border); color:var(--accent)">&#8250;</button>
        </div>
      </div>
    </div>

  </main>
  <app-footer />
</div>

<!-- Applicant Detail Slide-over -->
<div *ngIf="selectedApplicant" class="fixed inset-0 z-50 flex justify-end">
  <div class="absolute inset-0 bg-black/30" (click)="closeDetail()"></div>
  <div class="relative w-full max-w-md h-full shadow-2xl flex flex-col overflow-y-auto" style="background:var(--surface)">

    <!-- Header -->
    <div class="flex items-center justify-between px-6 py-4" style="border-bottom:1px solid var(--border)">
      <h2 class="font-bold" style="color:var(--text)">Applicant Profile</h2>
      <button (click)="closeDetail()" class="text-xl leading-none" style="color:var(--text-muted)">&times;</button>
    </div>

    <!-- Loading -->
    <div *ngIf="detailLoading" class="flex-1 flex items-center justify-center">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2" style="border-color:var(--accent)"></div>
    </div>

    <div *ngIf="!detailLoading" class="flex-1 px-6 py-5 space-y-5">

      <!-- Avatar + Basic Info -->
      <div class="flex items-center gap-4">
        <div class="w-14 h-14 rounded-full flex items-center justify-center text-white text-xl font-bold shrink-0 avatar-gradient">
          {{ selectedApplicant.userEmail?.charAt(0)?.toUpperCase() }}
        </div>
        <div>
          <p class="font-bold text-lg" style="color:var(--text)">{{ applicantProfile?.name || selectedApplicant.userEmail }}</p>
          <p class="text-sm" style="color:var(--text-muted)">{{ selectedApplicant.userEmail }}</p>
          <span class="badge mt-1 inline-block" [style]="getStatusStyle(selectedApplicant.status)">
            {{ getStatusLabel(selectedApplicant.status) }}
          </span>
        </div>
      </div>

      <!-- Application Info -->
      <div class="rounded-xl p-4 space-y-2" style="background:var(--bg); border:1px solid var(--border)">
        <div class="flex justify-between text-sm">
          <span style="color:var(--text-muted)">Applied</span>
          <span class="font-semibold" style="color:var(--text)">{{ selectedApplicant.appliedAt | date:'MMM dd, yyyy' }}</span>
        </div>
        <div class="flex justify-between text-sm">
          <span style="color:var(--text-muted)">Job</span>
          <span class="font-semibold" style="color:var(--text)">{{ selectedApplicant.jobTitle || 'Job #' + selectedApplicant.jobId }}</span>
        </div>
        <div *ngIf="applicantProfile?.mobile" class="flex justify-between text-sm">
          <span style="color:var(--text-muted)">Mobile</span>
          <span class="font-semibold" style="color:var(--text)">{{ applicantProfile.mobile }}</span>
        </div>
        <div class="flex justify-between text-sm">
          <span style="color:var(--text-muted)">App ID</span>
          <span class="font-semibold text-xs" style="color:var(--text)">#{{ selectedApplicant.applicationId?.substring(0,8) }}</span>
        </div>
      </div>

      <!-- Skills -->
      <div *ngIf="applicantProfile?.skills">
        <h3 class="font-bold mb-2" style="color:var(--text)">Skills</h3>
        <div class="flex flex-wrap gap-2">
          <span *ngFor="let s of applicantProfile.skills.split(',')" class="skill-tag">{{ s.trim() }}</span>
        </div>
      </div>

      <!-- About Me -->
      <div *ngIf="applicantProfile?.headline">
        <h3 class="font-bold mb-2" style="color:var(--text)">About Me</h3>
        <p class="text-sm rounded-xl p-3" style="color:var(--text); background:var(--bg); border:1px solid var(--border)">{{ applicantProfile.headline }}</p>
      </div>

      <!-- Resumes -->
      <div>
        <h3 class="font-bold mb-3" style="color:var(--text)">Resumes</h3>
        <div *ngIf="applicantResumes.length === 0" class="text-sm text-center py-4 rounded-xl" style="color:var(--text-muted); background:var(--bg); border:1px solid var(--border)">
          No resumes uploaded
        </div>
        <div *ngFor="let r of applicantResumes" class="flex items-center justify-between p-3 rounded-xl mb-2" style="background:var(--bg); border:1px solid var(--border)">
          <div class="flex items-center gap-3">
            <span class="text-xl" style="color:var(--accent)">📄</span>
            <div>
              <p class="text-sm font-semibold" style="color:var(--text)">{{ r.fileUrl?.split('/')?.pop() || 'Resume' }}</p>
              <p class="text-xs" style="color:var(--text-muted)">{{ r.uploadedAt | date:'MMM dd, yyyy' }}</p>
            </div>
          </div>
          <a [href]="getDownloadUrl(r.fileUrl)" target="_blank"
            class="text-sm font-semibold hover:underline" style="color:var(--accent)">View →</a>
        </div>
      </div>

      <!-- Update Status -->
      <div>
        <h3 class="font-bold mb-3" style="color:var(--text)">Update Status</h3>
        <select (change)="updateStatus(selectedApplicant.applicationId, $event)"
          class="w-full rounded-lg px-3 py-2.5 text-sm focus:outline-none"
          style="border:1px solid var(--border); background:var(--bg); color:var(--text)">
          <option value="APPLIED" [selected]="selectedApplicant.status === 'APPLIED'">Applied</option>
          <option value="SHORTLISTED" [selected]="selectedApplicant.status === 'SHORTLISTED'">Shortlisted</option>
          <option value="INTERVIEW_SCHEDULED" [selected]="selectedApplicant.status === 'INTERVIEW_SCHEDULED'">Interview Scheduled</option>
          <option value="REJECTED" [selected]="selectedApplicant.status === 'REJECTED'">Rejected</option>
        </select>
      </div>

    </div>
  </div>
</div>
  `
})
export class ApplicantsComponent implements OnInit {

  applications: any[] = [];
  loading = true;
  jobId = 0;
  searchTerm = '';
  statusFilter = '';
  currentPage = 0;
  totalPages = 0;
  totalElements = 0;
  pageSize = 10;
  selectedApplicant: any = null;
  applicantResumes: any[] = [];
  applicantProfile: any = null;
  detailLoading = false;

  constructor(
    public router: Router,
    private route: ActivatedRoute,
    private applicantService: ApplicantService,
    private recruiterJobService: RecruiterJobService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.jobId = Number(params['jobId']);
      if (this.jobId) {
        this.loadApplicants();
      } else {
        this.loadAllApplicants();
      }
    });
  }

  loadAllApplicants() {
    this.loading = true;
    this.recruiterJobService.getMyJobs(0, 500).subscribe({
      next: (res) => {
        const jobs = res.content || res;
        if (jobs.length === 0) { this.loading = false; this.cdr.detectChanges(); return; }
        const allApps: any[] = [];
        let pending = jobs.length;
        jobs.forEach((job: any) => {
          this.applicantService.getApplicants(job.jobId, 0, 500).subscribe({
            next: (r) => {
              const apps = r.content || r;
              apps.forEach((a: any) => { a.jobTitle = job.title; a.jobCompany = job.company; });
              allApps.push(...apps);
            },
            error: () => {},
          }).add(() => {
            pending--;
            if (pending === 0) {
              this.applications = allApps.sort((a, b) =>
                new Date(b.appliedAt).getTime() - new Date(a.appliedAt).getTime()
              );
              this.totalElements = this.applications.length;
              this.loading = false;
              this.cdr.detectChanges();
            }
          });
        });
      },
      error: () => { this.loading = false; this.cdr.detectChanges(); }
    });
  }

  loadApplicants() {
    this.loading = true;
    this.applicantService.getApplicants(this.jobId, this.currentPage, this.pageSize).subscribe({
      next: (res) => {
        this.applications = res.content || res;
        this.totalElements = res.totalElements || this.applications.length;
        this.totalPages = res.totalPages || 1;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => { this.loading = false; this.cdr.detectChanges(); }
    });
  }

  get filteredApplications() {
    return this.applications.filter(a => {
      const matchSearch = !this.searchTerm || a.userEmail?.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchStatus = !this.statusFilter || a.status === this.statusFilter;
      return matchSearch && matchStatus;
    });
  }

  get shortlistedCount() {
    return this.applications.filter(a => a.status === 'SHORTLISTED').length;
  }

  updateStatus(applicationId: string, event: any) {
    const status = event.target.value;
    this.applicantService.updateStatus(applicationId, status).subscribe({
      next: (res) => {
        this.applications = this.applications.map(a =>
          a.applicationId === applicationId ? { ...a, status: res.status || status } : a
        );
        this.totalElements = this.applications.length;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Status update failed', err);
        this.cdr.detectChanges();
      }
    });
  }

  getStatusStyle(status: string): string {
    const map: any = {
      'APPLIED':             'background:var(--accent-dim); color:var(--accent)',
      'SHORTLISTED':         'background:var(--warn-dim); color:var(--warn-text)',
      'INTERVIEW_SCHEDULED': 'background:#ede9fe; color:#5b21b6',
      'REJECTED':            'background:var(--danger-dim); color:var(--danger)'
    };
    return map[status] || 'background:var(--surface-alt); color:var(--text)';
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

  goToPage(page: number) {
    if (page < 0 || page >= this.totalPages) return;
    this.currentPage = page;
    this.loadApplicants();
  }

  openDetail(app: any) {
    this.selectedApplicant = app;
    this.applicantResumes = [];
    this.applicantProfile = null;
    this.detailLoading = true;
    let pending = 2;
    const done = () => { if (--pending === 0) { this.detailLoading = false; this.cdr.detectChanges(); } };
    this.applicantService.getResumesByEmail(app.userEmail).subscribe({
      next: (res) => { this.applicantResumes = res; done(); },
      error: () => done()
    });
    this.applicantService.getUserByEmail(app.userEmail).subscribe({
      next: (res) => { this.applicantProfile = res; done(); },
      error: () => done()
    });
  }

  closeDetail() { this.selectedApplicant = null; this.applicantResumes = []; this.applicantProfile = null; }

  getDownloadUrl(fileUrl: string): string {
    const filename = fileUrl?.split('/').pop();
    return `${this.applicantService['base']}/api/resumes/download/${filename}`;
  }

  getPages(): number[] {
    return Array.from({ length: Math.min(this.totalPages, 5) }, (_, i) => i);
  }

  exportCsv() {
    const rows = [['Candidate', 'Job', 'Applied Date', 'Status']]
      .concat(this.filteredApplications.map((a: any) => [
        a.userEmail,
        a.jobTitle || 'Job #' + a.jobId,
        a.appliedAt,
        a.status
      ]));
    const csv = rows.map(r => r.join(',')).join('\n');
    const anchor = document.createElement('a');
    anchor.href = 'data:text/csv;charset=utf-8,' + encodeURIComponent(csv);
    anchor.download = 'applicants.csv';
    anchor.click();
  }
}
