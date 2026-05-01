import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [RouterLink],
  template: `
    <footer class="border-t py-4 px-6 flex items-center justify-between text-xs mt-auto" style="background:var(--surface); border-color:var(--border); color:var(--accent)">
      <div class="flex items-center gap-2">
        <span class="text-lg">🧭</span>
        <span class="font-black tracking-widest uppercase text-xs" style="color:var(--text)">JOB<span style="color:var(--text)">COMPASS</span></span>
      </div>
      <span style="color:var(--text)">© 2026 JobCompass Inc. All rights reserved.</span>
      <div class="flex gap-4">
        <a routerLink="/about" class="transition" style="color:var(--text)" onmouseover="this.style.color='var(--accent)'" onmouseout="this.style.color='var(--accent)'">About</a>
        <a routerLink="/careers" class="transition" style="color:var(--text)" onmouseover="this.style.color='var(--accent)'" onmouseout="this.style.color='var(--accent)'">Careers</a>
        <a routerLink="/privacy" class="transition" style="color:var(--text)" onmouseover="this.style.color='var(--accent)'" onmouseout="this.style.color='var(--accent)'">Privacy</a>
        <a routerLink="/terms" class="transition" style="color:var(--text)" onmouseover="this.style.color='var(--accent)'" onmouseout="this.style.color='var(--accent)'">Terms</a>
        <a routerLink="/help" class="transition" style="color:var(--text)" onmouseover="this.style.color='var(--accent)'" onmouseout="this.style.color='var(--accent)'">Help</a>
      </div>
    </footer>
  `
})
export class FooterComponent {}
