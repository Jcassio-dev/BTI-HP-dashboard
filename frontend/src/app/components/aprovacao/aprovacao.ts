import { Component, OnInit, effect, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { BaseChartDirective } from 'ng2-charts';
import { Chart, ChartConfiguration, registerables } from 'chart.js';
import { ApiService, AprovacaoItem, MateriaData } from '../../api/api.service';
import { TituloPipe } from '../../texto/titulo.pipe';
import { TemaService } from '../../tema/tema.service';
import { coresDesfecho, token } from '../../tema/tokens';

@Component({
  selector: 'app-aprovacao',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, BaseChartDirective, TituloPipe],
  templateUrl: './aprovacao.html',
})
export class AprovacaoComponent implements OnInit {
  mode: 'disciplina' | 'docente' = 'disciplina';
  q = '';
  resultados: AprovacaoItem[] = [];
  loading = false;
  searched = false;
  private debounceTimer: ReturnType<typeof setTimeout> | undefined;

  turma: AprovacaoItem | null = null;
  materia: MateriaData | null = null;
  loadingMateria = false;
  aba: 'metricas' | 'equivalencias' = 'metricas';
  donut: ChartConfiguration<'doughnut'> | null = null;

  private readonly tema = inject(TemaService);

  constructor(
    private api: ApiService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    Chart.register(...registerables);
    effect(() => {
      this.tema.aplicado();
      if (this.turma) this.donut = this.montarDonut(this.turma);
    });
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

  abrirTurma(r: AprovacaoItem): void {
    this.aba = 'metricas';
    this.turma = r;
    this.donut = this.montarDonut(r);
    this.materia = null;
    this.loadingMateria = true;
    this.api.getMateria(r.componenteId).subscribe({
      next: (m) => {
        this.materia = m;
        this.loadingMateria = false;
      },
      error: () => {
        this.loadingMateria = false;
      },
    });
  }

  verOutrasTurmas(r: AprovacaoItem): void {
    const nome = r.componenteNome ?? '';
    this.fecharModal();
    this.mode = 'disciplina';
    this.q = nome;
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { disciplina: nome, professor: null },
      queryParamsHandling: 'merge',
      replaceUrl: true,
    });
    this.buscar();
  }

  fecharModal(): void {
    this.turma = null;
    this.materia = null;
    this.donut = null;
  }

  private montarDonut(r: AprovacaoItem): ChartConfiguration<'doughnut'> {
    const cores = coresDesfecho();
    return {
      type: 'doughnut',
      data: {
        labels: ['Aprovado', 'Reprovado (nota/média)', 'Reprovado por falta', 'Trancado'],
        datasets: [
          {
            data: [r.aprovados, r.reprovadosNota, r.reprovadosFalta, r.trancados],
            backgroundColor: [cores.aprovado, cores.reprovado, cores.falta, cores.trancado],
            borderColor: token('--card'),
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
}
