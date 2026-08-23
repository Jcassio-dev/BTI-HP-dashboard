import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { AnalyticsComponent } from '../components/analytics/analytics';
import { ApiService, ApiData, AnalyticsData, OverTimePoint } from '../api/api.service';
import { Sparkline } from '../sparkline/sparkline';

interface Barra {
  rotulo: string;
  valor: number;
  pct: number;
  primeira: boolean;
}

interface Resumo {
  valor: number;
  variacao: number | null;
  serie: number[];
}

const TOPO = 8;

export interface Periodo {
  dias: number;
  rotulo: string;
}

export const PERIODOS: Periodo[] = [
  { dias: 7, rotulo: '7 dias' },
  { dias: 30, rotulo: '30 dias' },
  { dias: 90, rotulo: '90 dias' },
  { dias: 0, rotulo: 'Tudo' },
];

@Component({
  selector: 'app-bot',
  standalone: true,
  imports: [DecimalPipe, AnalyticsComponent, Sparkline],
  templateUrl: './bot.html',
})
export class Bot implements OnInit {
  private readonly api = inject(ApiService);

  readonly dados = signal<ApiData | null>(null);
  readonly anterior = signal<ApiData | null>(null);
  readonly serie = signal<OverTimePoint[]>([]);
  readonly pronto = signal(false);
  readonly periodo = signal<number>(30);
  readonly periodos = PERIODOS;

  readonly barras = computed<Barra[]>(() => {
    const d = this.dados();
    if (!d) return [];
    const pares = Object.entries(d.counts).sort((a, b) => b[1] - a[1]);
    const topo = pares.slice(0, TOPO);
    const resto = pares.slice(TOPO).reduce((s, [, n]) => s + n, 0);
    const itens = resto > 0 ? [...topo, ['outros', resto] as [string, number]] : topo;
    const max = itens.length ? itens[0][1] : 1;
    return itens.map(([rotulo, valor], i) => ({
      rotulo,
      valor,
      pct: (valor / max) * 100,
      primeira: i === 0,
    }));
  });

  readonly comandos = computed<Resumo>(() =>
    this.resumo(
      this.dados()?.totalReceived ?? 0,
      this.anterior()?.totalReceived ?? null,
      (p) => p.commands
    )
  );

  /** Alunos distintos no periodo. Somar o distinto de cada dia contaria a mesma pessoa varias vezes. */
  readonly alunos = computed<Resumo>(() =>
    this.resumo(
      this.dados()?.differentUsers ?? 0,
      this.anterior()?.differentUsers ?? null,
      (p) => p.users
    )
  );

  readonly rotuloPeriodo = computed(
    () => PERIODOS.find((p) => p.dias === this.periodo())?.rotulo ?? 'Tudo'
  );

  ngOnInit(): void {
    this.carregar();
  }

  trocarPeriodo(dias: number): void {
    if (this.periodo() === dias) return;
    this.periodo.set(dias);
    this.carregar();
  }

  private carregar(): void {
    const dias = this.periodo();
    this.pronto.set(false);
    this.api.getData(dias).subscribe({
      next: (d) => {
        this.dados.set(d);
        this.pronto.set(true);
      },
      error: () => this.pronto.set(true),
    });

    if (dias > 0) {
      this.api.getData(dias, dias).subscribe({
        next: (d) => this.anterior.set(d),
        error: () => this.anterior.set(null),
      });
    } else {
      this.anterior.set(null);
    }
    this.api.getAnalytics(dias).subscribe({
      next: (a: AnalyticsData) => this.serie.set(a.overTime),
      error: () => this.serie.set([]),
    });
  }

  /** O valor vem do backend; a serie e so o desenho da sparkline. */
  private resumo(valor: number, antes: number | null, campo: (p: OverTimePoint) => number): Resumo {
    const valores = this.serie().map(campo);
    const janela = this.periodo() > 0 ? this.periodo() : valores.length;
    return {
      valor,
      variacao: antes !== null && antes > 0 ? ((valor - antes) / antes) * 100 : null,
      serie: valores.slice(-janela),
    };
  }
}
