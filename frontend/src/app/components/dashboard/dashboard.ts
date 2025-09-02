import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common'; // Necessário para ngIf, etc.
import { ApiService, ApiData } from '../../api/api.service';
import { BaseChartDirective } from 'ng2-charts'; // Importe a diretiva do ng2-charts
import { ChartOptions, ChartData, registerables, Chart } from 'chart.js';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, BaseChartDirective],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css'],
})
export class DashboardComponent implements OnInit {
  public totalComandos: number = 0;
  public totalUsuarios: number = 0;
  public chartReady: boolean = false;

  public barChartOptions: ChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      y: {
        beginAtZero: true,
        title: { display: true, text: 'Quantidade' },
      },
      x: {
        title: { display: true, text: 'Comandos' },
      },
    },
    plugins: {
      legend: {
        display: false,
      },
    },
  };

  public barChartData: ChartData<'bar'> = {
    labels: [],
    datasets: [
      {
        data: [],
        label: 'Número de Execuções',
        backgroundColor: 'rgba(54, 162, 235, 0.6)',
        borderColor: 'rgba(54, 162, 235, 1)',
        borderWidth: 1,
      },
    ],
  };

  constructor(private apiService: ApiService) {
    Chart.register(...registerables);
  }

  ngOnInit(): void {
    this.apiService.getData().subscribe((data: ApiData) => {
      this.totalComandos = data.totalReceived;
      this.totalUsuarios = data.differentUsers;

      this.barChartData.labels = Object.keys(data.counts);
      this.barChartData.datasets[0].data = Object.values(data.counts);

      this.chartReady = true;
    });
  }
}
