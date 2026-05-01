import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AiService {
  constructor(private http: HttpClient) {}

  analyzeResume(fileUrl: string): Observable<any> {
    return this.http.post(`${environment.apiUrl}/api/ai/analyze-resume`, { fileUrl });
  }

  chat(message: string, context: string): Observable<any> {
    return this.http.post(`${environment.apiUrl}/api/ai/chat`, { message, context });
  }
}
