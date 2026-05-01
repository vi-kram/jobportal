import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class RecruiterJobService {

  private base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  createJob(data: any): Observable<any> {
    return this.http.post(`${this.base}/api/jobs`, data);
  }

  updateJob(jobId: number, data: any): Observable<any> {
    return this.http.put(`${this.base}/api/jobs/${jobId}`, data);
  }

  getMyJobs(page = 0, size = 10): Observable<any> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get(`${this.base}/api/jobs`, { params });
  }

  closeJob(jobId: number): Observable<any> {
    return this.http.put(`${this.base}/api/jobs/${jobId}/close`, {});
  }

  getJobById(jobId: number): Observable<any> {
    return this.http.get(`${this.base}/api/jobs/${jobId}`);
  }
}
