import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ApiService, AprovacaoItem } from '../../api/api.service';

@Component({
  selector: 'app-aprovacao',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './aprovacao.html',
})
export class AprovacaoComponent implements OnInit {
  mode: 'disciplina' | 'docente' = 'disciplina';
  q = '';
  resultados: AprovacaoItem[] = [];
  loading = false;
  searched = false;

  constructor(private api: ApiService, private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe((p) => {
      if (p['disciplina'] !== undefined) {
        this.mode = 'disciplina';
        this.q = p['disciplina'];
        this.buscar();
      } else if (p['professor'] !== undefined) {
        this.mode = 'docente';
        this.q = p['professor'];
        this.buscar();
      }
    });
  }

  setMode(m: 'disciplina' | 'docente'): void {
    if (this.mode === m) return;
    this.mode = m;
    if (this.q.trim()) this.buscar();
  }

  buscar(): void {
    const termo = this.q.trim();
    if (!termo) {
      this.resultados = [];
      this.searched = false;
      return;
    }
    this.loading = true;
    this.searched = true;
    this.api.getAprovacao(this.mode, termo).subscribe({
      next: (r) => {
        this.resultados = r;
        this.loading = false;
      },
      error: () => {
        this.resultados = [];
        this.loading = false;
      },
    });
  }

  pct(taxa: number): number {
    return Math.round(taxa * 100);
  }

  corTexto(taxa: number): string {
    const p = taxa * 100;
    if (p >= 70) return 'text-green-600 dark:text-green-400';
    if (p >= 50) return 'text-yellow-600 dark:text-yellow-400';
    return 'text-red-600 dark:text-red-400';
  }

  corBarra(taxa: number): string {
    const p = taxa * 100;
    if (p >= 70) return 'bg-green-500';
    if (p >= 50) return 'bg-yellow-500';
    return 'bg-red-500';
  }
}
