import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

// A interface continua a mesma
export interface ApiData {
  counts: { [key: string]: number };
  totalReceived: number;
  differentUsers: number;
}

@Injectable({
  providedIn: 'root',
})
export class ApiService {
  private apiUrl = `${environment.apiUrl}/stats`;

  constructor(private http: HttpClient) {}

  getData(): Observable<ApiData> {
    return this.http.get<ApiData>(this.apiUrl);
  }
}
