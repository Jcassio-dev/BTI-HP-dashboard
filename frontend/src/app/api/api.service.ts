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

export interface OverTimePoint {
  date: string;
  commands: number;
  users: number;
}

export interface HourPoint {
  hour: number;
  count: number;
}

export interface ChatTypeCounts {
  group: number;
  private: number;
}

export interface AnalyticsData {
  overTime: OverTimePoint[];
  byHour: HourPoint[];
  chatType: ChatTypeCounts;
}

export interface AprovacaoItem {
  componenteId: number;
  componenteCodigo: string | null;
  componenteNome: string | null;
  docenteNome: string | null;
  aprovados: number;
  reprovados: number;
  total: number;
  taxa: number;
}

@Injectable({
  providedIn: 'root',
})
export class ApiService {
  private apiUrl = `${environment.apiUrl}/stats`;
  private analyticsUrl = `${environment.apiUrl}/analytics`;
  private apiRoot = environment.apiUrl.replace(/\/api\/logs\/?$/, '');

  constructor(private http: HttpClient) {}

  getData(): Observable<ApiData> {
    return this.http.get<ApiData>(this.apiUrl);
  }

  getAnalytics(): Observable<AnalyticsData> {
    return this.http.get<AnalyticsData>(this.analyticsUrl);
  }

  getAprovacao(kind: 'disciplina' | 'docente', q: string): Observable<AprovacaoItem[]> {
    return this.http.get<AprovacaoItem[]>(`${this.apiRoot}/api/aprovacao/${kind}`, { params: { q } });
  }
}
