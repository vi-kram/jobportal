import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class JobService {

  private base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getAllOpenJobs(page = 0, size = 9): Observable<any> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get(`${this.base}/search/all/jobs`, { params });
  }

  searchJobs(filters: any, page = 0, size = 10): Observable<any> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (filters.keyword) params = params.set('keyword', filters.keyword);
    if (filters.location) params = params.set('location', filters.location);
    if (filters.company) params = params.set('company', filters.company);
    if (filters.minSalary) params = params.set('minSalary', filters.minSalary);
    if (filters.maxSalary) params = params.set('maxSalary', filters.maxSalary);
    return this.http.get(`${this.base}/search/jobs`, { params });
  }

  getJobById(id: number): Observable<any> {
    return this.http.get(`${this.base}/search/jobs/${id}`);
  }
}
