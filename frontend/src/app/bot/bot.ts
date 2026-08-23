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
const JANELA = 30;

@Component({
  selector: 'app-bot',
  standalone: true,
  imports: [DecimalPipe, AnalyticsComponent, Sparkline],
  templateUrl: './bot.html',
})
export class Bot implements OnInit {
  private readonly api = inject(ApiService);

  readonly dados = signal<ApiData | null>(null);
  readonly serie = signal<OverTimePoint[]>([]);
  readonly pronto = signal(false);

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

  readonly comandos = computed<Resumo>(() => this.resumo((p) => p.commands));
  readonly usuarios = computed<Resumo>(() => this.resumo((p) => p.users));

  ngOnInit(): void {
    this.api.getData().subscribe({
      next: (d) => {
        this.dados.set(d);
        this.pronto.set(true);
      },
      error: () => this.pronto.set(true),
    });
    this.api.getAnalytics().subscribe({
      next: (a: AnalyticsData) => this.serie.set(a.overTime),
      error: () => this.serie.set([]),
    });
  }

  private resumo(campo: (p: OverTimePoint) => number): Resumo {
    const pontos = this.serie();
    const valores = pontos.map(campo);
    const atual = valores.slice(-JANELA);
    const anterior = valores.slice(-JANELA * 2, -JANELA);
    const soma = (v: number[]) => v.reduce((s, n) => s + n, 0);
    const total = soma(atual);
    const antes = soma(anterior);
    return {
      valor: total,
      variacao: antes > 0 ? ((total - antes) / antes) * 100 : null,
      serie: atual,
    };
  }
}
