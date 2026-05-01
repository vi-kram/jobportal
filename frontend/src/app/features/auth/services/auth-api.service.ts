import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthApiService {

  private base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  login(email: string, password: string): Observable<{ token: string }> {
    return this.http.post<{ token: string }>(`${this.base}/api/users/login`, { email, password });
  }

  register(data: any): Observable<any> {
    return this.http.post(`${this.base}/api/users/register`, data);
  }
}
