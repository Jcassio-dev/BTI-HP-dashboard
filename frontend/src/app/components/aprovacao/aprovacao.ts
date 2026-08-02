import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { BaseChartDirective } from 'ng2-charts';
import { Chart, ChartConfiguration, registerables } from 'chart.js';
import { ApiService, AprovacaoItem, MateriaData } from '../../api/api.service';

@Component({
  selector: 'app-aprovacao',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, BaseChartDirective],
  templateUrl: './aprovacao.html',
})
export class AprovacaoComponent implements OnInit {
  mode: 'disciplina' | 'docente' = 'disciplina';
  q = '';
  resultados: AprovacaoItem[] = [];
  loading = false;
  searched = false;
  private debounceTimer: ReturnType<typeof setTimeout> | undefined;

  materia: MateriaData | null = null;
  loadingMateria = false;
  aba: 'metricas' | 'equivalencias' = 'metricas';
  donut: ChartConfiguration<'doughnut'> | null = null;

  private isDark =
    window.matchMedia?.('(prefers-color-scheme: dark)').matches ?? false;

  constructor(private api: ApiService, private route: ActivatedRoute) {
    Chart.register(...registerables);
  }

  onInput(): void {
    clearTimeout(this.debounceTimer);
    this.debounceTimer = setTimeout(() => this.buscar(), 300);
  }

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

  abrirMateria(id: number): void {
    this.aba = 'metricas';
    this.loadingMateria = true;
    this.materia = null;
    this.donut = null;
    this.api.getMateria(id).subscribe({
      next: (m) => {
        this.materia = m;
        this.donut = this.montarDonut(m);
        this.loadingMateria = false;
      },
      error: () => {
        this.loadingMateria = false;
      },
    });
  }

  fecharModal(): void {
    this.materia = null;
    this.donut = null;
  }

  private montarDonut(m: MateriaData): ChartConfiguration<'doughnut'> {
    const surface = this.isDark ? '#1f2937' : '#ffffff';
    return {
      type: 'doughnut',
      data: {
        labels: ['Aprovado', 'Reprovado (nota/média)', 'Reprovado por falta', 'Trancado'],
        datasets: [
          {
            data: [m.aprovado, m.reprovadoNota, m.reprovadoFalta, m.trancado],
            backgroundColor: ['#22c55e', '#ef4444', '#f59e0b', '#9ca3af'],
            borderColor: surface,
            borderWidth: 2,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: { enabled: true },
        },
      },
    };
  }

  linhas(texto: string | null): string[] {
    return (texto ?? '').split('\n').map((l) => l.trim()).filter((l) => l.length > 0);
  }

  pct(taxa: number): number {
    return Math.round(taxa * 100);
  }

  pctBreakdown(valor: number, total: number): number {
    return total > 0 ? Math.round((valor / total) * 100) : 0;
  }

  corTexto(taxa: number): string {
    const p = taxa * 100;
    if (p >= 70) return 'text-green-600 dark:text-green-400';
    if (p >= 50) return 'text-yellow-600 dark:text-yellow-400';
    return 'text-red-600 dark:text-red-400';
  }

  corBarra(taxa: number): string {
    const p = taxa * 100;
    if (p >= 70) return '#22c55e';
    if (p >= 50) return '#eab308';
    return '#ef4444';
  }
}
