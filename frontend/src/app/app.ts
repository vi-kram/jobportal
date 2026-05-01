import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ChatbotComponent } from './shared/components/chatbot/chatbot.component';
import { AuthService } from './core/services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ChatbotComponent, CommonModule],
  template: `
    <router-outlet />
    <app-chatbot *ngIf="isLoggedIn" />
  `
})
export class App {
  constructor(public authService: AuthService) {}
  get isLoggedIn() { return this.authService.isLoggedIn(); }
}
