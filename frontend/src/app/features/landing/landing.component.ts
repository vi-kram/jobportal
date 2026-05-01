import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
<div class="min-h-screen flex flex-col" style="background:var(--bg)">

  <!-- Navbar -->
  <nav class="w-full px-8 py-4 flex items-center justify-between sticky top-0 z-50 shadow-sm" style="background:var(--surface); border-bottom:1px solid var(--border)">
    <div class="flex items-center gap-2 cursor-pointer" (click)="scrollToTop()">
      <span class="text-2xl">🧭</span>
      <span class="text-lg tracking-widest uppercase font-black" style="color:var(--text)">JOB<span style="color:var(--text)">COMPASS</span><span style="color:var(--accent); font-size:0.6rem; vertical-align:super">▲</span></span>
    </div>
    <div class="flex items-center gap-3">
      <a routerLink="/login" class="text-sm font-medium px-4 py-2 rounded-full transition" style="color:var(--text)">Sign In</a>
      <a routerLink="/register" class="text-sm font-semibold text-white px-5 py-2 rounded-full transition shadow-md" style="background:var(--accent)">Get Started</a>
    </div>
  </nav>

  <!-- Hero -->
  <section class="flex flex-col items-center justify-center text-center px-6 pt-24 pb-20" style="background:linear-gradient(160deg,var(--bg) 0%,#d8d8d0 100%)">
    <span class="text-xs font-semibold px-4 py-1.5 rounded-full mb-6 tracking-wide uppercase" style="background:var(--surface); color:var(--accent)">🧭 Your Career Starts Here</span>
    <h1 class="text-5xl font-extrabold leading-tight max-w-3xl mb-6" style="color:var(--text)">
      Find Your <span style="color:var(--text)">Dream Job</span><br/>or Hire Top Talent
    </h1>
    <p class="text-lg max-w-xl mb-10" style="color:var(--text)">
      <span class="tracking-widest uppercase text-base font-black" style="color:var(--text)">JOB<span style="color:var(--text)">COMPASS</span></span> connects ambitious job seekers with great companies. Apply in seconds, track your progress, and land the role you deserve.
    </p>
    <div class="flex flex-col sm:flex-row gap-4 justify-center">
      <a routerLink="/register" class="text-white font-semibold px-8 py-3.5 rounded-full text-sm transition shadow-lg" style="background:var(--accent)">
        Find a Job →
      </a>
      <a routerLink="/register" class="font-semibold px-8 py-3.5 rounded-full text-sm transition" style="border:2px solid var(--accent); color:var(--accent)">
        Post a Job →
      </a>
    </div>
  </section>

  <!-- Stats -->
  <section class="py-12" style="background:var(--accent)">
    <div class="max-w-4xl mx-auto grid grid-cols-2 sm:grid-cols-4 gap-8 text-center px-6">
      <div *ngFor="let stat of stats">
        <p class="text-3xl font-extrabold text-white">{{ stat.display }}</p>
        <p class="text-sm mt-1" style="color:var(--surface)">{{ stat.label }}</p>
      </div>
    </div>
  </section>

  <!-- For Job Seekers -->
  <section class="py-20 px-6 max-w-6xl mx-auto w-full">
    <div class="text-center mb-12">
      <span class="text-xs font-semibold px-4 py-1.5 rounded-full uppercase tracking-wide" style="background:var(--surface); color:var(--accent)">👤 For Job Seekers</span>
      <h2 class="text-3xl font-bold mt-4" style="color:var(--text)">Everything you need to land your next role</h2>
    </div>
    <div class="grid grid-cols-1 sm:grid-cols-3 gap-6">
      <div *ngFor="let f of seekerFeatures" class="rounded-2xl p-6 shadow-sm card-hover" style="background:var(--surface); border:1px solid var(--border)">
        <div class="w-12 h-12 rounded-xl flex items-center justify-center text-2xl mb-4" style="background:var(--bg)">{{ f.icon }}</div>
        <h3 class="font-bold mb-2" style="color:var(--text)">{{ f.title }}</h3>
        <p class="text-sm leading-relaxed" style="color:var(--text)">{{ f.desc }}</p>
      </div>
    </div>
  </section>

  <!-- For Recruiters -->
  <section class="py-20 px-6" style="background:var(--surface)">
    <div class="max-w-6xl mx-auto w-full">
      <div class="text-center mb-12">
        <span class="text-xs font-semibold px-4 py-1.5 rounded-full uppercase tracking-wide" style="background:var(--bg); color:var(--accent)">🏢 For Recruiters</span>
        <h2 class="text-3xl font-bold mt-4" style="color:var(--text)">Hire smarter, faster</h2>
      </div>
      <div class="grid grid-cols-1 sm:grid-cols-3 gap-6">
        <div *ngFor="let f of recruiterFeatures" class="rounded-2xl p-6 shadow-sm card-hover" style="background:var(--bg); border:1px solid var(--border)">
          <div class="w-12 h-12 rounded-xl flex items-center justify-center text-2xl mb-4" style="background:var(--surface)">{{ f.icon }}</div>
          <h3 class="font-bold mb-2" style="color:var(--text)">{{ f.title }}</h3>
          <p class="text-sm leading-relaxed" style="color:var(--text)">{{ f.desc }}</p>
        </div>
      </div>
    </div>
  </section>

  <!-- How It Works -->
  <section class="py-20 px-6 max-w-5xl mx-auto w-full">
    <div class="text-center mb-14">
      <span class="text-xs font-semibold px-4 py-1.5 rounded-full uppercase tracking-wide" style="background:var(--surface); color:var(--accent)">⚡ How It Works</span>
      <h2 class="text-3xl font-bold mt-4" style="color:var(--text)">Get started in 3 simple steps</h2>
    </div>
    <div class="grid grid-cols-1 sm:grid-cols-3 gap-8">
      <div *ngFor="let step of steps; let i = index" class="flex flex-col items-center text-center">
        <div class="w-14 h-14 rounded-full text-white flex items-center justify-center text-xl font-bold mb-4 shadow-lg" style="background:var(--accent)">
          {{ i + 1 }}
        </div>
        <h3 class="font-bold mb-2" style="color:var(--text)">{{ step.title }}</h3>
        <p class="text-sm leading-relaxed" style="color:var(--text)">{{ step.desc }}</p>
      </div>
    </div>
  </section>

  <!-- Bottom CTA -->
  <section class="py-20 px-6 text-center" style="background:var(--accent)">
    <h2 class="text-3xl font-extrabold text-white mb-4">Ready to take the next step?</h2>
    <p class="mb-8 text-sm max-w-md mx-auto" style="color:var(--surface)">Join thousands of professionals who found their dream job or hired top talent through <span class="font-bold text-white">JobCompass</span>.</p>
    <div class="flex flex-col sm:flex-row gap-4 justify-center">
      <a routerLink="/register" class="font-bold px-8 py-3.5 rounded-full text-sm transition shadow-lg" style="background:var(--bg); color:var(--accent)">
        Create Free Account
      </a>
      <a routerLink="/login" class="font-semibold px-8 py-3.5 rounded-full text-sm transition" style="border:2px solid var(--surface); color:white">
        Sign In
      </a>
    </div>
  </section>

  <!-- Footer -->
  <footer class="py-8 text-center text-xs" style="border-top:1px solid var(--surface); color:var(--accent)">
    &copy; 2026 <span class="tracking-widest uppercase font-black" style="color:var(--text)">JOB<span style="color:var(--text)">COMPASS</span></span>. All rights reserved.
  </footer>

</div>
  `,
})
export class LandingComponent implements OnInit {

  constructor(private cdr: ChangeDetectorRef) {}

  stats = [
    { target: 500, current: 0, display: '0', label: 'Jobs Posted', suffix: '+' },
    { target: 200, current: 0, display: '0', label: 'Companies', suffix: '+' },
    { target: 1000, current: 0, display: '0', label: 'Job Seekers', suffix: '+' },
    { target: 350, current: 0, display: '0', label: 'Hired', suffix: '+' },
  ];

  ngOnInit() {
    setTimeout(() => this.animateStats(), 300);
  }

  scrollToTop() { window.scrollTo({ top: 0, behavior: 'smooth' }); }

  animateStats() {
    const duration = 2000;
    const steps = 60;
    const interval = duration / steps;
    let step = 0;
    const timer = setInterval(() => {
      step++;
      const progress = step / steps;
      const ease = 1 - Math.pow(1 - progress, 3);
      this.stats.forEach(stat => {
        stat.current = Math.round(stat.target * ease);
        const val = stat.current >= 1000 ? (stat.current / 1000).toFixed(0) + ',000' : stat.current.toString();
        stat.display = val + (progress >= 1 ? stat.suffix : '');
      });
      this.cdr.detectChanges();
      if (step >= steps) clearInterval(timer);
    }, interval);
  }

  seekerFeatures = [
    { icon: '🔍', title: 'Smart Job Search', desc: 'Filter by title, location, salary, and job type to find roles that match exactly what you\'re looking for.' },
    { icon: '⚡', title: 'One-Click Apply', desc: 'Apply to jobs instantly with your saved profile and resume. No repetitive form filling.' },
    { icon: '📋', title: 'Track Applications', desc: 'See real-time status updates — Applied, Shortlisted, Interview, or Rejected — all in one place.' },
    { icon: '📄', title: 'Resume Builder', desc: 'Upload your resume and let our AI analyzer give you tips to improve it for better results.' },
    { icon: '🤖', title: 'AI Assistant', desc: 'Ask our AI chatbot anything about your applications, profile completeness, or available jobs.' },
    { icon: '🔖', title: 'Bookmark Jobs', desc: 'Save interesting jobs and come back to apply when you\'re ready.' },
  ];

  recruiterFeatures = [
    { icon: '📝', title: 'Post Jobs Easily', desc: 'Create detailed job listings with title, description, salary, location, and job type in minutes.' },
    { icon: '👥', title: 'Manage Applicants', desc: 'Review applicants, view their resumes and skills, and update their status with a single click.' },
    { icon: '📊', title: 'Dashboard Analytics', desc: 'Get a clear overview of your open roles, total applicants, shortlisted candidates, and more.' },
    { icon: '📤', title: 'Export Data', desc: 'Download applicant data as CSV for offline review or sharing with your hiring team.' },
    { icon: '🤖', title: 'AI Assistant', desc: 'Ask the AI chatbot about your posted jobs, applicant counts, and hiring pipeline.' },
    { icon: '🔒', title: 'Close & Reopen Jobs', desc: 'Control your listings — close a role when filled and reopen it anytime you need more candidates.' },
  ];

  steps = [
    { title: 'Create Your Account', desc: 'Sign up as a Job Seeker or Recruiter in under a minute. No credit card required.' },
    { title: 'Build Your Profile', desc: 'Add your skills, upload your resume, and complete your profile to stand out to recruiters.' },
    { title: 'Get Hired or Hire', desc: 'Apply to jobs and track your progress, or post roles and find your perfect candidate.' },
  ];
}
