import { Component, computed, input } from '@angular/core';

@Component({
  selector: 'bti-sparkline',
  standalone: true,
  template: `
    <svg [attr.viewBox]="'0 0 ' + largura + ' ' + altura" [attr.width]="largura" [attr.height]="altura"
         fill="none" aria-hidden="true" class="block">
      <polyline [attr.points]="pontos()" stroke="var(--ink-3)" stroke-width="1.25"
                stroke-linejoin="round" stroke-linecap="round" />
    </svg>
  `,
})
export class Sparkline {
  readonly valores = input.required<number[]>();
  readonly largura = 88;
  readonly altura = 24;

  readonly pontos = computed(() => {
    const v = this.valores();
    if (v.length < 2) return '';
    const max = Math.max(...v);
    const min = Math.min(...v);
    const faixa = max - min || 1;
    const passo = this.largura / (v.length - 1);
    const margem = 2;
    const util = this.altura - margem * 2;
    return v
      .map((n, i) => `${(i * passo).toFixed(2)},${(margem + util - ((n - min) / faixa) * util).toFixed(2)}`)
      .join(' ');
  });
}
