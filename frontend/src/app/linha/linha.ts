import { Component, computed, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AprovacaoItem } from '../api/api.service';
import { Faixa } from '../faixa/faixa';
import { TituloPipe } from '../texto/titulo.pipe';

const BOT = '558486735862';

@Component({
  selector: 'bti-linha',
  standalone: true,
  imports: [Faixa, TituloPipe, RouterLink],
  templateUrl: './linha.html',
})
export class Linha {
  readonly item = input.required<AprovacaoItem>();
  readonly maxTotal = input.required<number>();
  readonly aberta = input(false);
  readonly mostrarDisciplina = input(true);
  readonly mostrarProfessor = input(true);

  readonly alternar = output<void>();

  readonly pequena = computed(() => this.item().totalMatriculados < 20);
  readonly pct = computed(() => Math.round(this.item().taxaAprovacao * 100));

  readonly linkBot = computed(() => {
    const nome = this.item().docenteNome ?? '';
    return `https://wa.me/${BOT}?text=${encodeURIComponent('!professor ' + nome)}`;
  });

  readonly idDetalhe = computed(
    () => `d-${this.item().componenteId}-${this.item().docenteSlug ?? 'x'}`
  );

  parte(valor: number, total: number): number {
    return total > 0 ? Math.round((valor / total) * 100) : 0;
  }
}
