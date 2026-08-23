import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { ApiService, AprovacaoItem, TurmaData } from '../api/api.service';
import { FaixaLegenda } from '../faixa/faixa-legenda';
import { Linha } from '../linha/linha';
import { Rodape } from '../rodape/rodape';
import { TituloPipe } from '../texto/titulo.pipe';
import { Meta, Title } from '@angular/platform-browser';
import { metaDaPagina } from '../seo/meta';
import { tituloCase } from '../texto/titulo';

@Component({
  selector: 'app-turma',
  standalone: true,
  imports: [DecimalPipe, RouterLink, FaixaLegenda, Linha, Rodape, TituloPipe],
  templateUrl: './turma.html',
})
export class Turma implements OnInit {
  private readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly title = inject(Title);
  private readonly meta = inject(Meta);

  readonly turma = signal<TurmaData | null>(null);
  readonly carregando = signal(true);
  readonly aberta = signal<string | null>(null);
  readonly maxTotal = signal(0);

  ngOnInit(): void {
    const codigo = this.route.snapshot.paramMap.get('codigo') ?? '';
    this.api.getTurma(codigo, 'taxa').subscribe({
      next: (t) => {
        this.turma.set(t);
        this.maxTotal.set(t.professores.reduce((m, i) => Math.max(m, i.totalMatriculados), 0));
        this.carregando.set(false);
        metaDaPagina(this.title, this.meta, {
          titulo: `${t.codigo} ${tituloCase(t.nome)}`.trim(),
          descricao: `${Math.round(t.taxaAprovacao * 100)}% de aprovação em ${t.professores.length} turmas, ${t.totalMatriculados} alunos.`,
          caminho: `/turma/${t.codigo}`,
          og: `/api/og?tipo=turma&codigo=${encodeURIComponent(t.codigo ?? '')}`,
        });
      },
      error: () => {
        this.turma.set(null);
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

  linhas(texto: string | null): string[] {
    return (texto ?? '').split('\n').map((l) => l.trim()).filter((l) => l.length > 0);
  }
}
