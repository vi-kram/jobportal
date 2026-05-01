import { Component, OnInit, ChangeDetectorRef, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpParams } from '@angular/common/http';
import { AuthService } from '../../../core/services/auth.service';
import { AiService } from '../../../features/job-seeker/services/ai.service';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-chatbot',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
<!-- Floating Button -->
<button (click)="toggleChat()"
  class="fixed bottom-6 right-6 z-50 w-14 h-14 bg-blue-700 hover:bg-blue-800 text-white rounded-full shadow-lg flex items-center justify-center transition-all">
  <span *ngIf="!isOpen" class="text-2xl">🧭</span>
  <span *ngIf="isOpen" class="text-xl font-bold">✕</span>
</button>

<!-- Chat Panel -->
<div *ngIf="isOpen"
  class="fixed bottom-24 right-6 z-50 w-96 bg-white  rounded-2xl shadow-2xl border border-gray-200  flex flex-col overflow-hidden"
  style="height: 520px;">

  <!-- Header -->
  <div class="bg-blue-700 px-4 py-3 flex items-center gap-3">
    <div class="w-8 h-8 bg-white/20 rounded-full flex items-center justify-center text-white text-sm font-bold">J</div>
    <div>
      <p class="text-white font-semibold text-sm">JobPortal Assistant</p>
      <p class="text-blue-200 text-xs">Ask me anything about your account</p>
    </div>
  </div>

  <!-- Messages -->
  <div #messagesContainer class="flex-1 overflow-y-auto px-4 py-3 space-y-3 bg-gray-50 ">
    <div *ngFor="let msg of messages" [class]="msg.role === 'user' ? 'flex justify-end' : 'flex justify-start'">
      <div [class]="msg.role === 'user'
        ? 'bg-blue-700 text-white text-sm px-4 py-2.5 rounded-2xl rounded-br-sm max-w-xs'
        : 'bg-white  border border-gray-200  text-gray-800  text-sm px-4 py-2.5 rounded-2xl rounded-bl-sm max-w-xs shadow-sm'">
        {{ msg.text }}
      </div>
    </div>
    <div *ngIf="loading" class="flex justify-start">
      <div class="bg-white  border border-gray-200  px-4 py-2.5 rounded-2xl rounded-bl-sm shadow-sm">
        <div class="flex gap-1 items-center">
          <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay:0s"></div>
          <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay:0.15s"></div>
          <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay:0.3s"></div>
        </div>
      </div>
    </div>
  </div>

  <!-- Suggestions -->
  <div *ngIf="messages.length === 1" class="px-4 py-2 flex flex-wrap gap-2 bg-gray-50  border-t border-gray-100 ">
    <button *ngFor="let s of suggestions" (click)="sendSuggestion(s)"
      class="text-xs bg-blue-50 border border-blue-200 text-blue-700 px-3 py-1.5 rounded-full hover:bg-blue-100 transition">
      {{ s }}
    </button>
  </div>

  <!-- Input -->
  <div class="px-4 py-3 border-t border-gray-100  bg-white  flex gap-2">
    <input [(ngModel)]="inputText" (keydown.enter)="send()" type="text"
      placeholder="Ask something..."
      class="flex-1 border border-gray-200    rounded-full px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
    <button (click)="send()" [disabled]="loading || !inputText.trim()"
      class="bg-blue-700 hover:bg-blue-800 text-white w-9 h-9 rounded-full flex items-center justify-center disabled:opacity-50 shrink-0">
      ➤
    </button>
  </div>
</div>
  `
})
export class ChatbotComponent implements OnInit {

  @ViewChild('messagesContainer') messagesContainer!: ElementRef;

  isOpen = false;
  loading = false;
  inputText = '';
  messages: { role: string; text: string }[] = [];
  context = '';

  constructor(
    private authService: AuthService,
    private aiService: AiService,
    private http: HttpClient,
    private cdr: ChangeDetectorRef
  ) {}

  get role() { return this.authService.getRole(); }
  get email() { return this.authService.getEmail(); }

  get suggestions(): string[] {
    if (this.role === 'RECRUITER') {
      return ['How many jobs have I posted?', 'How many applicants do I have?', 'Show my open jobs'];
    }
    return ['How many jobs have I applied to?', 'Show remote jobs', 'What is my application status?', 'Is my profile complete?'];
  }

  ngOnInit() {
    this.messages = [{ role: 'bot', text: `Hi! I'm your JobPortal assistant. Ask me anything about your ${this.role === 'RECRUITER' ? 'job postings and applicants' : 'applications and profile'}.` }];
  }

  toggleChat() {
    this.isOpen = !this.isOpen;
    if (this.isOpen && !this.context) {
      this.loadContext();
    }
  }

  loadContext() {
    const base = environment.apiUrl;
    if (this.role === 'RECRUITER') {
      this.http.get<any>(`${base}/api/jobs`, { params: new HttpParams().set('page', 0).set('size', 100) }).subscribe({
        next: (jobsRes) => {
          const jobs = jobsRes.content || jobsRes;
          const open = jobs.filter((j: any) => j.status === 'OPEN').length;
          const closed = jobs.filter((j: any) => j.status === 'CLOSED').length;
          let ctx = `RECRUITER DASHBOARD DATA for ${this.email}:\n`;
          ctx += `Total jobs posted: ${jobs.length}\n`;
          ctx += `Open jobs: ${open}\n`;
          ctx += `Closed jobs: ${closed}\n`;
          ctx += `Jobs list:\n`;
          jobs.forEach((j: any) => {
            ctx += `- "${j.title}" at ${j.company}, ${j.location}, Status: ${j.status}, Salary: ${j.salary || 'Not specified'}\n`;
          });

          // Now load applicants for each job
          let totalApplicants = 0;
          let shortlisted = 0;
          let interviews = 0;
          let pending = jobs.length;
          const allApplicants: any[] = [];
          if (jobs.length === 0) { this.context = ctx; this.cdr.detectChanges(); return; }
          jobs.forEach((j: any) => {
            this.http.get<any>(`${base}/api/applications/job/${j.jobId}`, {
              params: new HttpParams().set('page', 0).set('size', 100)
            }).subscribe({
              next: (r) => {
                const apps = r.content || r;
                totalApplicants += r.totalElements || apps.length;
                shortlisted += apps.filter((a: any) => a.status === 'SHORTLISTED').length;
                interviews += apps.filter((a: any) => a.status === 'INTERVIEW_SCHEDULED').length;
                apps.forEach((a: any) => allApplicants.push({ ...a, jobTitle: j.title }));
              },
              error: () => {},
            }).add(() => {
              pending--;
              if (pending === 0) {
                ctx += `\nAPPLICANT STATS:\nTotal applicants: ${totalApplicants}\nShortlisted: ${shortlisted}\nInterviews scheduled: ${interviews}\n`;
                ctx += `\nAPPLICANT LIST:\n`;
                allApplicants.forEach((a: any) => {
                  ctx += `- ${a.userEmail}, Job: "${a.jobTitle}", Status: ${a.status}\n`;
                });
                this.context = ctx.substring(0, 4000);
                this.cdr.detectChanges();
              }
            });
          });
        }
      });
    } else {
      this.http.get<any>(`${base}/api/applications/me`, { params: new HttpParams().set('page', 0).set('size', 100) }).subscribe({
        next: (appsRes) => {
          const apps = appsRes.content || appsRes;
          const applied = apps.filter((a: any) => a.status === 'APPLIED').length;
          const shortlisted = apps.filter((a: any) => a.status === 'SHORTLISTED').length;
          const interviews = apps.filter((a: any) => a.status === 'INTERVIEW_SCHEDULED').length;
          const rejected = apps.filter((a: any) => a.status === 'REJECTED').length;
          let ctx = `JOB SEEKER DATA for ${this.email}:\n`;
          ctx += `Total applications: ${apps.length}\n`;
          ctx += `Applied: ${applied}, Shortlisted: ${shortlisted}, Interviews: ${interviews}, Rejected: ${rejected}\n`;
          ctx += `Applications:\n`;
          apps.forEach((a: any) => {
            ctx += `- Job ID ${a.jobId}, Status: ${a.status}, Applied: ${a.appliedAt}\n`;
          });
          this.http.get<any>(`${base}/api/users/me`).subscribe({
            next: (user) => {
              ctx += `\nPROFILE:\nName: ${user.name || 'Not set'}\nMobile: ${user.mobile || 'Not set'}\nSkills: ${user.skills || 'Not set'}\nAbout: ${user.headline || 'Not set'}\n`;
              // Also load available jobs
              this.http.get<any>(`${base}/api/jobs`, { params: new HttpParams().set('page', 0).set('size', 200) }).subscribe({
                next: (jobsRes) => {
                  const jobs = jobsRes.content || jobsRes;
                  const openJobs = jobs.filter((j: any) => j.status === 'OPEN');
                  // Deduplicate by title+company
                  const seen = new Set<string>();
                  const uniqueJobs = openJobs.filter((j: any) => {
                    const key = j.title + '|' + j.company;
                    if (seen.has(key)) return false;
                    seen.add(key);
                    return true;
                  });
                  ctx += `\nAVAILABLE JOBS (${uniqueJobs.length} unique open jobs):\n`;
                  uniqueJobs.slice(0, 30).forEach((j: any) => {
                    ctx += `- "${j.title}" at ${j.company}, Location: ${j.location}, Type: ${j.jobType || 'Not specified'}, Salary: ${j.salary ? '$' + j.salary : 'Not specified'}\n`;
                  });
                  if (uniqueJobs.length > 30) ctx += `...and ${uniqueJobs.length - 30} more jobs.\n`;
                  this.context = ctx;
                  this.cdr.detectChanges();
                },
                error: () => { this.context = ctx; this.cdr.detectChanges(); }
              });
            }
          });
        }
      });
    }
  }

  send() {
    const text = this.inputText.trim();
    if (!text || this.loading) return;
    this.inputText = '';
    this.messages.push({ role: 'user', text });
    this.loading = true;
    this.cdr.detectChanges();
    this.scrollToBottom();

    this.aiService.chat(text, this.context.substring(0, 3000)).subscribe({
      next: (res) => {
        console.log('Chat response:', res);
        this.messages.push({ role: 'bot', text: res.reply || 'Sorry, I could not understand that.' });
        this.loading = false;
        this.cdr.detectChanges();
        this.scrollToBottom();
      },
      error: (err) => {
        console.error('Chat error:', err);
        this.messages.push({ role: 'bot', text: 'Sorry, something went wrong. Please try again.' });
        this.loading = false;
        this.cdr.detectChanges();
        this.scrollToBottom();
      }
    });
  }

  sendSuggestion(text: string) {
    this.inputText = text;
    this.send();
  }

  scrollToBottom() {
    setTimeout(() => {
      if (this.messagesContainer) {
        this.messagesContainer.nativeElement.scrollTop = this.messagesContainer.nativeElement.scrollHeight;
      }
    }, 50);
  }
}
