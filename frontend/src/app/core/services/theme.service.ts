import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private dark = false;

  constructor() {
    const saved = localStorage.getItem('theme');
    this.dark = saved === 'dark';
    this.apply();
  }

  get isDark() { return this.dark; }

  toggle() {
    this.dark = !this.dark;
    localStorage.setItem('theme', this.dark ? 'dark' : 'light');
    this.apply();
  }

  private apply() {
    document.documentElement.classList.toggle('dark', this.dark);
  }
}
