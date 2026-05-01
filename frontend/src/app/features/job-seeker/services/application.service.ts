import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ApplicationService {

  private base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  applyForJob(jobId: number): Observable<any> {
    return this.http.post(`${this.base}/api/applications`, { jobId });
  }

  getMyApplications(page = 0, size = 10): Observable<any> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get(`${this.base}/api/applications/me`, { params });
  }

  withdrawApplication(applicationId: string): Observable<any> {
    return this.http.delete(`${this.base}/api/applications/${applicationId}`);
  }
}
