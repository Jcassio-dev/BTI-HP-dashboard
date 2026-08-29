import { Routes } from '@angular/router';
import { Busca } from './busca/busca';

export const routes: Routes = [
  { path: '', component: Busca },
  { path: 'turma/:codigo', loadComponent: () => import('./turma/turma').then((m) => m.Turma) },
  {
    path: 'professor/:slug',
    loadComponent: () => import('./professor/professor').then((m) => m.Professor),
  },
  { path: 'conectar', loadComponent: () => import('./conectar/conectar').then((m) => m.Conectar) },
  { path: 'termos', loadComponent: () => import('./termos/termos').then((m) => m.Termos) },
  { path: 'bot', loadChildren: () => import('./bot/bot.routes').then((m) => m.routes) },
  { path: 'aprovacao', redirectTo: '', pathMatch: 'full' },
  { path: 'dashboard', redirectTo: 'bot', pathMatch: 'full' },
  {
    path: '**',
    loadComponent: () => import('./nao-encontrado/nao-encontrado').then((m) => m.NaoEncontrado),
  },
];
