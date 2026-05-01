import { Component, OnInit, AfterViewInit, ChangeDetectorRef, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from '../../../../shared/components/navbar/navbar.component';
import { FooterComponent } from '../../../../shared/components/footer/footer.component';
import { AnalyticsService } from '../../services/analytics.service';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  selector: 'app-analytics-dashboard',
  standalone: true,
  imports: [CommonModule, NavbarComponent, FooterComponent],
  template: `
<div class="min-h-screen flex flex-col bg-gray-50">
  <app-navbar />
  <main class="flex-1 max-w-6xl mx-auto w-full px-6 py-8">

    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Analytics Dashboard</h1>
        <p class="text-sm text-gray-500 mt-1">Platform overview and system health</p>
      </div>
      <button class="border border-gray-200 text-gray-700 px-4 py-2 rounded-lg text-sm font-medium hover:bg-gray-50">
        ⬇ Export Report
      </button>
    </div>

    <!-- Loading -->
    <div *ngIf="loading" class="grid grid-cols-4 gap-4 mb-6">
      <div *ngFor="let i of [1,2,3,4]" class="bg-white rounded-xl p-5 animate-pulse h-28"></div>
    </div>

    <!-- Stats Cards -->
    <div *ngIf="!loading" class="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <div class="bg-white rounded-xl border border-gray-100 p-5">
        <div class="flex items-center justify-between mb-2">
          <p class="text-sm text-gray-500">Jobs Created</p>
          <span class="text-blue-600 text-xl">💼</span>
        </div>
        <p class="text-3xl font-bold text-gray-900">{{ summary['JOB_CREATED'] || 0 }}</p>
        <p class="text-xs text-green-500 mt-1">↑ Total</p>
      </div>
      <div class="bg-white rounded-xl border border-gray-100 p-5">
        <div class="flex items-center justify-between mb-2">
          <p class="text-sm text-gray-500">Jobs Applied</p>
          <span class="text-green-600 text-xl">📋</span>
        </div>
        <p class="text-3xl font-bold text-gray-900">{{ summary['JOB_APPLIED'] || 0 }}</p>
        <p class="text-xs text-green-500 mt-1">↑ Total</p>
      </div>
      <div class="bg-white rounded-xl border border-gray-100 p-5">
        <div class="flex items-center justify-between mb-2">
          <p class="text-sm text-gray-500">Resumes Uploaded</p>
          <span class="text-purple-600 text-xl">📄</span>
        </div>
        <p class="text-3xl font-bold text-gray-900">{{ summary['RESUME_UPLOADED'] || 0 }}</p>
        <p class="text-xs text-purple-500 mt-1">Total</p>
      </div>
      <div class="bg-white rounded-xl border border-gray-100 p-5">
        <div class="flex items-center justify-between mb-2">
          <p class="text-sm text-gray-500">Jobs Closed</p>
          <span class="text-red-500 text-xl">🔒</span>
        </div>
        <p class="text-3xl font-bold text-gray-900">{{ summary['JOB_CLOSED'] || 0 }}</p>
        <p class="text-xs text-red-500 mt-1">Total</p>
      </div>
    </div>

    <!-- Charts Row -->
    <div *ngIf="!loading" class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">

      <!-- Bar Chart - Top Jobs -->
      <div class="bg-white rounded-xl border border-gray-100 p-6">
        <h2 class="font-bold text-gray-900 mb-4">Top Jobs by Applications</h2>
        <div *ngIf="jobMetrics.length === 0" class="text-center py-8 text-gray-400 text-sm">No data yet</div>
        <canvas #jobChart *ngIf="jobMetrics.length > 0" height="200"></canvas>
      </div>

      <!-- Doughnut Chart - Application Funnel -->
      <div class="bg-white rounded-xl border border-gray-100 p-6">
        <h2 class="font-bold text-gray-900 mb-4">Platform Activity</h2>
        <canvas #summaryChart height="200"></canvas>
      </div>

    </div>

    <!-- User Metrics Table -->
    <div *ngIf="!loading" class="bg-white rounded-xl border border-gray-100 p-6">
      <h2 class="font-bold text-gray-900 mb-4">Top Users by Applications</h2>
      <div *ngIf="userMetrics.length === 0" class="text-center py-8 text-gray-400 text-sm">No data yet</div>
      <div *ngIf="userMetrics.length > 0" class="space-y-3">
        <div *ngFor="let u of userMetrics" class="flex items-center gap-4">
          <div class="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center text-blue-700 text-xs font-bold shrink-0">
            {{ u.userEmail?.charAt(0)?.toUpperCase() }}
          </div>
          <span class="text-sm text-gray-700 w-48 truncate">{{ u.userEmail }}</span>
          <div class="flex-1 bg-gray-100 rounded-full h-2">
            <div class="bg-green-500 h-2 rounded-full transition-all" [style.width.%]="getUserBarWidth(u.applicationCount)"></div>
          </div>
          <span class="text-sm font-semibold text-gray-800 w-8 text-right">{{ u.applicationCount }}</span>
        </div>
      </div>
    </div>

  </main>
  <app-footer />
</div>
  `
})
export class AnalyticsDashboardComponent implements OnInit, AfterViewInit {

  @ViewChild('jobChart') jobChartRef!: ElementRef;
  @ViewChild('summaryChart') summaryChartRef!: ElementRef;

  summary: any = {};
  jobMetrics: any[] = [];
  userMetrics: any[] = [];
  loading = true;
  maxUserCount = 1;
  chartsReady = false;

  constructor(
    private analyticsService: AnalyticsService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() { this.loadAll(); }

  ngAfterViewInit() { this.chartsReady = true; }

  loadAll() {
    this.loading = true;
    this.analyticsService.getSummary().subscribe({
      next: (res) => {
        this.summary = res;
        this.loading = false;
        this.cdr.detectChanges();
        setTimeout(() => this.renderCharts(), 100);
      },
      error: () => { this.loading = false; this.cdr.detectChanges(); }
    });

    this.analyticsService.getJobMetrics(0, 10).subscribe({
      next: (res) => {
        this.jobMetrics = res.content || res;
        this.cdr.detectChanges();
        setTimeout(() => this.renderJobChart(), 200);
      }
    });

    this.analyticsService.getUserMetrics(0, 10).subscribe({
      next: (res) => {
        this.userMetrics = res.content || res;
        this.maxUserCount = Math.max(...this.userMetrics.map((u: any) => u.applicationCount), 1);
        this.cdr.detectChanges();
      }
    });
  }

  renderCharts() {
    this.renderSummaryChart();
  }

  renderJobChart() {
    if (!this.jobChartRef || this.jobMetrics.length === 0) return;
    new Chart(this.jobChartRef.nativeElement, {
      type: 'bar',
      data: {
        labels: this.jobMetrics.map((j: any) => 'Job #' + j.jobId),
        datasets: [{
          label: 'Applications',
          data: this.jobMetrics.map((j: any) => j.applicationCount),
          backgroundColor: '#3B82F6',
          borderRadius: 6
        }]
      },
      options: {
        responsive: true,
        plugins: { legend: { display: false } },
        scales: { y: { beginAtZero: true } }
      }
    });
  }

  renderSummaryChart() {
    if (!this.summaryChartRef) return;
    new Chart(this.summaryChartRef.nativeElement, {
      type: 'doughnut',
      data: {
        labels: ['Jobs Created', 'Jobs Applied', 'Resumes Uploaded', 'Jobs Closed'],
        datasets: [{
          data: [
            this.summary['JOB_CREATED'] || 0,
            this.summary['JOB_APPLIED'] || 0,
            this.summary['RESUME_UPLOADED'] || 0,
            this.summary['JOB_CLOSED'] || 0
          ],
          backgroundColor: ['#3B82F6', '#10B981', '#8B5CF6', '#EF4444'],
          borderWidth: 0
        }]
      },
      options: {
        responsive: true,
        plugins: { legend: { position: 'bottom' } }
      }
    });
  }

  getUserBarWidth(count: number): number {
    return Math.round((count / this.maxUserCount) * 100);
  }
}
