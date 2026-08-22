import { Component, computed, inject } from '@angular/core';
import { Tema, TemaService } from './tema.service';

const ROTULOS: Record<Tema, string> = {
  sistema: 'Sistema',
  claro: 'Claro',
  escuro: 'Escuro',
};

@Component({
  selector: 'bti-tema-toggle',
  standalone: true,
  template: `
    <button
      type="button"
      (click)="tema.alternar()"
      [attr.aria-label]="descricao()"
      class="t-label px-2.5 py-1.5 rounded-sm border border-rule-2 bg-card hover:bg-paper transition-control duration-[120ms]">
      {{ rotulo() }}
    </button>
  `,
})
export class TemaToggle {
  readonly tema = inject(TemaService);
  readonly rotulo = computed(() => ROTULOS[this.tema.preferencia()]);
  readonly descricao = computed(
    () => `Tema: ${ROTULOS[this.tema.preferencia()].toLowerCase()}. Trocar para ${ROTULOS[this.tema.proximo()].toLowerCase()}.`
  );
}
