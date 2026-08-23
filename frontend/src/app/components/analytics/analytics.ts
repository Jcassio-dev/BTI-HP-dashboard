import { Component, effect, inject, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, Chart, registerables } from 'chart.js';
import { ApiService, AnalyticsData, OverTimePoint } from '../../api/api.service';
import { TemaService } from '../../tema/tema.service';
import { token } from '../../tema/tokens';

interface Palette {
  series1: string;
  series2: string;
  text: string;
  muted: string;
  grid: string;
  surface: string;
}

function paleta(): Palette {
  return {
    series1: token('--ink'),
    series2: token('--ink-3'),
    text: token('--ink-2'),
    muted: token('--ink-3'),
    grid: token('--rule'),
    surface: token('--card'),
  };
}

@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [CommonModule, BaseChartDirective],
  templateUrl: './analytics.html',
})
export class AnalyticsComponent {
  readonly dias = input(0);
  public ready = false;
  private readonly tema = inject(TemaService);
  private p: Palette = paleta();
  private dados?: AnalyticsData;

  public overTimeCommands!: ChartConfiguration<'line'>;
  public overTimeUsers!: ChartConfiguration<'line'>;
  public byHour!: ChartConfiguration<'bar'>;
  public chatType!: ChartConfiguration<'doughnut'>;

  constructor(private apiService: ApiService) {
    Chart.register(...registerables);
    effect(() => {
      this.tema.tema();
      this.p = paleta();
      if (this.dados) this.montar(this.dados);
    });

    effect(() => {
      const dias = this.dias();
      this.ready = false;
      this.apiService.getAnalytics(dias).subscribe((data: AnalyticsData) => {
        this.dados = data;
        this.montar(data);
      });
    });
  }

  private montar(data: AnalyticsData): void {
      const filled = this.fillDailyGaps(data.overTime);
      const dateLabels = filled.map((d) => this.formatDate(d.date));

      this.overTimeCommands = this.lineChart(
        dateLabels,
        filled.map((d) => d.commands),
        'Comandos por dia',
        this.p.series1,
      );

      this.overTimeUsers = this.lineChart(
        dateLabels,
        filled.map((d) => d.users),
        'Usuários ativos por dia',
        this.p.series2,
      );

      this.byHour = this.hourChart(data.byHour.map((h) => h.count));

      this.chatType = this.doughnutChart(data.chatType.group, data.chatType.private);

    this.ready = true;
  }

  // Preenche dias sem uso com zero pra a linha nao "pular" buracos.
  private fillDailyGaps(points: OverTimePoint[]): OverTimePoint[] {
    if (points.length === 0) return [];
    const byDate = new Map(points.map((p) => [p.date, p]));
    const result: OverTimePoint[] = [];
    const cursor = new Date(points[0].date + 'T00:00:00Z');
    const end = new Date(points[points.length - 1].date + 'T00:00:00Z');
    while (cursor <= end) {
      const iso = cursor.toISOString().slice(0, 10);
      result.push(byDate.get(iso) ?? { date: iso, commands: 0, users: 0 });
      cursor.setUTCDate(cursor.getUTCDate() + 1);
    }
    return result;
  }

  private formatDate(iso: string): string {
    const [, m, d] = iso.split('-');
    return `${d}/${m}`;
  }

  private lineChart(
    labels: string[],
    data: number[],
    label: string,
    color: string,
  ): ChartConfiguration<'line'> {
    return {
      type: 'line',
      data: {
        labels,
        datasets: [
          {
            data,
            label,
            borderColor: color,
            backgroundColor: color + '22',
            borderWidth: 2,
            fill: true,
            tension: 0.25,
            pointRadius: 0,
            pointHoverRadius: 5,
            pointHoverBackgroundColor: color,
          },
        ],
      },
      options: this.baseOptions(false),
    };
  }

  private hourChart(counts: number[]): ChartConfiguration<'bar'> {
    return {
      type: 'bar',
      data: {
        labels: Array.from({ length: 24 }, (_, h) => `${h}h`),
        datasets: [
          {
            data: counts,
            label: 'Comandos',
            backgroundColor: this.p.series1,
            borderRadius: 4,
            borderSkipped: false,
          },
        ],
      },
      options: this.baseOptions(false),
    };
  }

  private doughnutChart(group: number, priv: number): ChartConfiguration<'doughnut'> {
    return {
      type: 'doughnut',
      data: {
        labels: ['Grupos', 'Privado'],
        datasets: [
          {
            data: [group, priv],
            backgroundColor: [this.p.series1, this.p.series2],
            borderColor: this.p.surface,
            borderWidth: 2,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: 'bottom', labels: { color: this.p.text } },
          tooltip: { enabled: true },
        },
      },
    };
  }

  private baseOptions(showLegend: boolean): ChartConfiguration<'line' | 'bar'>['options'] {
    return {
      responsive: true,
      maintainAspectRatio: false,
      interaction: { mode: 'index', intersect: false },
      plugins: {
        legend: { display: showLegend, labels: { color: this.p.text } },
        tooltip: { enabled: true },
      },
      scales: {
        y: {
          beginAtZero: true,
          ticks: { color: this.p.muted, precision: 0 },
          grid: { color: this.p.grid },
        },
        x: {
          ticks: { color: this.p.muted, maxRotation: 0, autoSkip: true, maxTicksLimit: 12 },
          grid: { display: false },
        },
      },
    };
  }
}
