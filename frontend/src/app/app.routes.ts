import { Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard';
import { AprovacaoComponent } from './components/aprovacao/aprovacao';

export const routes: Routes = [
  { path: '', component: DashboardComponent },
  { path: 'aprovacao', component: AprovacaoComponent },
];
