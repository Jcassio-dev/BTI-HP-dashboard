import { Component } from '@angular/core';

@Component({
  selector: 'bti-faixa-legenda',
  standalone: true,
  template: `
    <div class="flex flex-wrap items-center gap-x-4 gap-y-1.5 t-meta">
      @for (i of itens; track i.rotulo) {
        <span class="flex items-center gap-1.5">
          <span class="w-3 h-3 rounded-sm inline-block" [style.background]="i.cor"></span>
          {{ i.rotulo }}
        </span>
      }
      <span>a largura da faixa é o total de matriculados</span>
    </div>
  `,
})
export class FaixaLegenda {
  readonly itens = [
    { cor: 'var(--aprovado)', rotulo: 'aprovado' },
    { cor: 'var(--reprovado)', rotulo: 'reprovado por nota' },
    { cor: 'var(--falta)', rotulo: 'reprovado por falta' },
    { cor: 'var(--trancado)', rotulo: 'trancou' },
  ];
}
