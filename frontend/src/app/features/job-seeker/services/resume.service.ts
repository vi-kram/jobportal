import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, switchMap } from 'rxjs';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ResumeService {

  private base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getMyResumes(): Observable<any> {
    return this.http.get(`${this.base}/api/resumes/me`);
  }

  uploadResumeUrl(fileUrl: string): Observable<any> {
    return this.http.post(`${this.base}/api/resumes`, { fileUrl });
  }

  uploadResumeFile(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(`${this.base}/api/resumes/upload`, formData);
  }

  deleteResume(id: number): Observable<any> {
    return this.http.delete(`${this.base}/api/resumes/${id}`);
  }

  getProfile(): Observable<any> {
    return this.http.get(`${this.base}/api/users/me`);
  }

  updateProfile(id: number, data: any): Observable<any> {
    return this.http.put(`${this.base}/api/users/${id}`, data);
  }

  updateMe(data: any): Observable<any> {
    return this.http.get<any>(`${this.base}/api/users/me`).pipe(
      switchMap((user: any) => this.http.put(`${this.base}/api/users/${user.id}`, data))
    );
  }
}
