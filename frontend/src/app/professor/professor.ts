import { Component, OnInit, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Meta, Title } from '@angular/platform-browser';
import { ApiService, AprovacaoItem, ProfessorData } from '../api/api.service';
import { FaixaLegenda } from '../faixa/faixa-legenda';
import { Linha } from '../linha/linha';
import { Rodape } from '../rodape/rodape';
import { TituloPipe } from '../texto/titulo.pipe';
import { tituloCase } from '../texto/titulo';
import { metaDaPagina } from '../seo/meta';

@Component({
  selector: 'app-professor',
  standalone: true,
  imports: [DecimalPipe, RouterLink, FaixaLegenda, Linha, Rodape, TituloPipe],
  templateUrl: './professor.html',
})
export class Professor implements OnInit {
  private readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly title = inject(Title);
  private readonly meta = inject(Meta);

  readonly professor = signal<ProfessorData | null>(null);
  readonly carregando = signal(true);
  readonly aberta = signal<string | null>(null);
  readonly maxTotal = signal(0);

  ngOnInit(): void {
    const slug = this.route.snapshot.paramMap.get('slug') ?? '';
    this.api.getProfessor(slug, 'taxa').subscribe({
      next: (p) => {
        this.professor.set(p);
        this.maxTotal.set(p.turmas.reduce((m, i) => Math.max(m, i.totalMatriculados), 0));
        this.carregando.set(false);
        metaDaPagina(this.title, this.meta, {
          titulo: tituloCase(p.nome),
          descricao: `${Math.round(p.taxaAprovacao * 100)}% de aprovação em ${p.turmas.length} disciplinas, ${p.totalMatriculados} alunos.`,
          caminho: `/professor/${p.slug}`,
          og: `/api/og?tipo=professor&slug=${encodeURIComponent(p.slug)}`,
        });
      },
      error: () => {
        this.professor.set(null);
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
}
