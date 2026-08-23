import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-nao-encontrado',
  standalone: true,
  template: `
    <div class="max-w-4xl mx-auto px-6 py-8">
      <h1 class="t-pagina text-ink mb-2">Página não encontrada</h1>
      <p class="t-apoio mb-6 max-w-[60ch]">Busque pelo código da disciplina ou pelo nome do professor.</p>
      <div class="flex gap-2 max-w-lg">
        <input
          type="search"
          [value]="q()"
          (input)="q.set($any($event.target).value)"
          (keyup.enter)="buscar()"
          placeholder="Código, disciplina ou nome do professor"
          class="flex-1 px-4 py-2 rounded-sm border border-rule-2 bg-card text-ink" />
        <button type="button" (click)="buscar()"
                class="px-5 py-2 rounded-sm bg-ink text-paper text-apoio transition-control duration-[120ms]">
          Buscar
        </button>
      </div>
    </div>
  `,
})
export class NaoEncontrado {
  private readonly router = inject(Router);
  readonly q = signal('');

  buscar(): void {
    const termo = this.q().trim();
    this.router.navigate(['/'], { queryParams: termo ? { q: termo } : {} });
  }
}
