import { Component, computed, inject } from '@angular/core';
import { TemaService } from './tema.service';

@Component({
  selector: 'bti-tema-toggle',
  standalone: true,
  template: `
    <button
      type="button"
      (click)="tema.alternar()"
      [attr.aria-label]="descricao()"
      class="p-2 rounded-sm border border-rule-2 bg-card text-ink hover:bg-paper transition-control duration-[120ms]">
      @if (escuro()) {
        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor"
             stroke-width="1.75" stroke-linecap="round" aria-hidden="true">
          <circle cx="12" cy="12" r="4.2" />
          <path d="M12 2.5v2.2M12 19.3v2.2M4.22 4.22l1.56 1.56M18.22 18.22l1.56 1.56M2.5 12h2.2M19.3 12h2.2M4.22 19.78l1.56-1.56M18.22 5.78l1.56-1.56" />
        </svg>
      } @else {
        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor"
             stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <path d="M20.5 14.3A8.6 8.6 0 0 1 9.7 3.5a8.6 8.6 0 1 0 10.8 10.8Z" />
        </svg>
      }
    </button>
  `,
})
export class TemaToggle {
  readonly tema = inject(TemaService);
  readonly escuro = computed(() => this.tema.tema() === 'escuro');
  readonly descricao = computed(() =>
    this.escuro() ? 'Mudar para o tema claro' : 'Mudar para o tema escuro'
  );
}
