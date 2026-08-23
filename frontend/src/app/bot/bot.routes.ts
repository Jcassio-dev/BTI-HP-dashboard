import { Routes } from '@angular/router';
import { provideCharts } from 'ng2-charts';

export const routes: Routes = [
  {
    path: '',
    providers: [provideCharts()],
    loadComponent: () => import('./bot').then((m) => m.Bot),
  },
];
