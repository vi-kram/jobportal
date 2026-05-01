import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AnalyticsService {

  private base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getSummary(): Observable<any> {
    return this.http.get(`${this.base}/analytics/summary`);
  }

  getJobMetrics(page = 0, size = 10): Observable<any> {
    return this.http.get(`${this.base}/analytics/jobs?page=${page}&size=${size}`);
  }

  getUserMetrics(page = 0, size = 10): Observable<any> {
    return this.http.get(`${this.base}/analytics/users?page=${page}&size=${size}`);
  }
}
