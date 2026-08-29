import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  { path: '', renderMode: RenderMode.Prerender },
  { path: 'turma/:codigo', renderMode: RenderMode.Server },
  { path: 'professor/:slug', renderMode: RenderMode.Server },
  { path: 'conectar', renderMode: RenderMode.Client },
  { path: 'bot', renderMode: RenderMode.Client },
  { path: 'aprovacao', renderMode: RenderMode.Prerender },
  { path: 'dashboard', renderMode: RenderMode.Prerender },
  { path: '**', renderMode: RenderMode.Prerender },
];
