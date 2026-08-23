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
  docenteSlug: string | null;
  aprovados: number;
  reprovadosNota: number;
  reprovadosFalta: number;
  trancados: number;
  totalAvaliados: number;
  totalMatriculados: number;
  taxaAprovacao: number;
}

export interface Busca {
  disciplinas: AprovacaoItem[];
  professores: AprovacaoItem[];
}

export interface TurmaData {
  id: number;
  codigo: string | null;
  nome: string | null;
  setor: string | null;
  cargaHoraria: number | null;
  ementa: string | null;
  equivalencias: string | null;
  preRequisito: string | null;
  coRequisito: string | null;
  aprovados: number;
  reprovadosNota: number;
  reprovadosFalta: number;
  trancados: number;
  totalAvaliados: number;
  totalMatriculados: number;
  taxaAprovacao: number;
  professores: AprovacaoItem[];
}

export interface ProfessorData {
  slug: string;
  nome: string | null;
  aprovados: number;
  reprovadosNota: number;
  reprovadosFalta: number;
  trancados: number;
  totalAvaliados: number;
  totalMatriculados: number;
  taxaAprovacao: number;
  turmas: AprovacaoItem[];
}

export interface Destaque {
  codigo: string | null;
  nome: string | null;
  totalMatriculados: number;
  taxaAprovacao: number;
}

export interface MateriaData {
  id: number;
  codigo: string | null;
  nome: string | null;
  setor: string | null;
  cargaHoraria: number | null;
  ementa: string | null;
  equivalencias: string | null;
  preRequisito: string | null;
  coRequisito: string | null;
  aprovados: number;
  reprovadosNota: number;
  reprovadosFalta: number;
  trancados: number;
  totalAvaliados: number;
  totalMatriculados: number;
  taxaAprovacao: number;
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

  getMateria(id: number): Observable<MateriaData> {
    return this.http.get<MateriaData>(`${this.apiRoot}/api/materia/${id}`);
  }

  getAprovacao(kind: 'disciplina' | 'docente', q: string): Observable<AprovacaoItem[]> {
    return this.http.get<AprovacaoItem[]>(`${this.apiRoot}/api/aprovacao/${kind}`, { params: { q } });
  }

  buscar(q: string, min: number, ordem: string): Observable<Busca> {
    return this.http.get<Busca>(`${this.apiRoot}/api/busca`, { params: { q, min, ordem } });
  }

  getTurma(codigo: string, ordem: string): Observable<TurmaData> {
    return this.http.get<TurmaData>(`${this.apiRoot}/api/turma/${encodeURIComponent(codigo)}`, {
      params: { ordem },
    });
  }

  getProfessor(slug: string, ordem: string): Observable<ProfessorData> {
    return this.http.get<ProfessorData>(`${this.apiRoot}/api/professor/${encodeURIComponent(slug)}`, {
      params: { ordem },
    });
  }

  getDestaques(): Observable<Destaque[]> {
    return this.http.get<Destaque[]>(`${this.apiRoot}/api/destaques`);
  }
}
