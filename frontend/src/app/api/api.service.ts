import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

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
  private apiUrl = `https://bti-api-532272487553.northamerica-south1.run.app/api/logs/stats`;

  constructor(private http: HttpClient) {}

  getData(): Observable<ApiData> {
    return this.http.get<ApiData>(this.apiUrl);
  }
}
