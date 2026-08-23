import { Component, OnInit, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiService, AprovacaoItem, Destaque } from '../api/api.service';
import { FaixaLegenda } from '../faixa/faixa-legenda';
import { Linha } from '../linha/linha';
import { Rodape } from '../rodape/rodape';
import { TituloPipe } from '../texto/titulo.pipe';

export type Ordem = 'taxa' | 'alunos' | 'nome';

const ESPERA = 250;
const MIN_AMOSTRA = 20;

@Component({
  selector: 'app-busca',
  standalone: true,
  imports: [DecimalPipe, FormsModule, RouterLink, FaixaLegenda, Linha, Rodape, TituloPipe],
  templateUrl: './busca.html',
})
export class Busca implements OnInit {
  private readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly q = signal('');
  readonly ordem = signal<Ordem>('taxa');
  readonly soGrandes = signal(false);

  readonly disciplinas = signal<AprovacaoItem[]>([]);
  readonly professores = signal<AprovacaoItem[]>([]);
  readonly destaques = signal<Destaque[]>([]);
  readonly carregando = signal(false);
  readonly buscou = signal(false);
  readonly aberta = signal<string | null>(null);
  readonly maxTotal = signal(0);

  readonly ordens: { valor: Ordem; rotulo: string }[] = [
    { valor: 'taxa', rotulo: 'Taxa' },
    { valor: 'alunos', rotulo: 'Nº de alunos' },
    { valor: 'nome', rotulo: 'Nome' },
  ];

  private timer: ReturnType<typeof setTimeout> | undefined;

  ngOnInit(): void {
    const p = this.route.snapshot.queryParamMap;
    this.q.set(p.get('q') ?? '');
    this.ordem.set((p.get('ordem') as Ordem) ?? 'taxa');
    this.soGrandes.set(p.get('min') === String(MIN_AMOSTRA));

    this.api.getDestaques().subscribe({
      next: (d) => this.destaques.set(d),
      error: () => this.destaques.set([]),
    });

    if (this.q().trim()) this.buscar();
  }

  aoDigitar(valor: string): void {
    this.q.set(valor);
    clearTimeout(this.timer);
    this.timer = setTimeout(() => this.buscar(), ESPERA);
  }

  trocarOrdem(o: Ordem): void {
    this.ordem.set(o);
    this.buscar();
  }

  trocarAmostra(marcado: boolean): void {
    this.soGrandes.set(marcado);
    this.buscar();
  }

  buscar(): void {
    clearTimeout(this.timer);
    const termo = this.q().trim();
    this.espelharUrl();

    if (!termo) {
      this.disciplinas.set([]);
      this.professores.set([]);
      this.buscou.set(false);
      this.maxTotal.set(0);
      return;
    }

    this.carregando.set(true);
    this.buscou.set(true);
    this.api.buscar(termo, this.soGrandes() ? MIN_AMOSTRA : 0, this.ordem()).subscribe({
      next: (r) => {
        this.disciplinas.set(r.disciplinas);
        this.professores.set(r.professores);
        this.maxTotal.set(
          [...r.disciplinas, ...r.professores].reduce((m, i) => Math.max(m, i.totalMatriculados), 0)
        );
        this.aberta.set(null);
        this.carregando.set(false);
      },
      error: () => {
        this.disciplinas.set([]);
        this.professores.set([]);
        this.maxTotal.set(0);
        this.carregando.set(false);
      },
    });
  }

  alternar(item: AprovacaoItem): void {
    const id = this.chave(item);
    this.aberta.set(this.aberta() === id ? null : id);
  }

  estaAberta(item: AprovacaoItem): boolean {
    return this.aberta() === this.chave(item);
  }

  chave(item: AprovacaoItem): string {
    return `${item.componenteId}-${item.docenteSlug ?? ''}`;
  }

  temResultado(): boolean {
    return this.disciplinas().length > 0 || this.professores().length > 0;
  }

  private espelharUrl(): void {
    const termo = this.q().trim();
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        q: termo || null,
        ordem: this.ordem() === 'taxa' ? null : this.ordem(),
        min: this.soGrandes() ? MIN_AMOSTRA : null,
      },
      replaceUrl: true,
    });
  }
}
