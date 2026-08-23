import { Component, computed, input } from '@angular/core';

function n(v: number): number {
  return Number.isFinite(v) ? v : 0;
}

export interface Segmento {
  cor: string;
  pct: number;
}

interface Desfecho {
  cor: string;
  n: number;
  singular: string;
  plural: string;
}

@Component({
  selector: 'bti-faixa',
  standalone: true,
  template: `
    <span class="faixa" role="img" [attr.aria-label]="descricao()" [style.--fracao]="fracao()">
      @for (s of segmentos(); track $index) {
        <span [style.background]="s.cor" [style.width.%]="s.pct"></span>
      }
    </span>
  `,
  styles: [
    `
      :host {
        display: inline-block;
      }

      .faixa {
        --trilho: 132px;
        display: flex;
        height: 16px;
        width: max(22px, calc(var(--fracao) * var(--trilho)));
        border-radius: 2px;
        overflow: hidden;
        background: var(--rule);
      }

      .faixa > span + span {
        border-left: 1px solid var(--card);
      }

      @media (max-width: 659px) {
        .faixa {
          --trilho: 100px;
        }
      }
    `,
  ],
})
export class Faixa {
  readonly aprovados = input.required<number>();
  readonly reprovadosNota = input.required<number>();
  readonly reprovadosFalta = input.required<number>();
  readonly trancados = input.required<number>();
  readonly maxTotal = input.required<number>();

  readonly total = computed(
    () => n(this.aprovados()) + n(this.reprovadosNota()) + n(this.reprovadosFalta()) + n(this.trancados())
  );

  private readonly desfechos = computed<Desfecho[]>(() => [
    { cor: 'var(--aprovado)', n: n(this.aprovados()), singular: 'aprovado', plural: 'aprovados' },
    { cor: 'var(--reprovado)', n: n(this.reprovadosNota()), singular: 'reprovado por nota', plural: 'reprovados por nota' },
    { cor: 'var(--falta)', n: n(this.reprovadosFalta()), singular: 'por falta', plural: 'por falta' },
    { cor: 'var(--trancado)', n: n(this.trancados()), singular: 'trancou', plural: 'trancaram' },
  ]);

  readonly segmentos = computed<Segmento[]>(() => {
    const total = this.total();
    if (total === 0) return [];
    return this.desfechos().map((d) => ({ cor: d.cor, pct: (d.n / total) * 100 }));
  });

  readonly fracao = computed(() => {
    const max = n(this.maxTotal());
    if (max <= 0 || this.total() <= 0) return 0;
    return Math.sqrt(this.total() / max);
  });

  readonly descricao = computed(() => {
    const total = this.total();
    if (total === 0) return 'sem matriculados';
    const partes = this.desfechos()
      .filter((d) => d.n > 0)
      .map((d) => `${d.n} ${d.n === 1 ? d.singular : d.plural}`);
    return `${total} ${total === 1 ? 'matriculado' : 'matriculados'}: ${partes.join(', ')}`;
  });
}
