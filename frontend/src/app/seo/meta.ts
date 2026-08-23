import { Meta, Title } from '@angular/platform-browser';

export const SITE = 'https://bti-hp-dashboard.vercel.app';

export interface MetaPagina {
  titulo: string;
  descricao: string;
  caminho: string;
  og?: string;
}

export function metaDaPagina(title: Title, meta: Meta, p: MetaPagina): void {
  const url = SITE + p.caminho;
  const imagem = SITE + (p.og ?? '/api/og');

  title.setTitle(`${p.titulo} · BTI help`);

  const tags: Record<string, string> = {
    description: p.descricao,
    'og:type': 'website',
    'og:site_name': 'BTI help',
    'og:title': p.titulo,
    'og:description': p.descricao,
    'og:url': url,
    'og:image': imagem,
    'og:image:width': '1200',
    'og:image:height': '630',
    'twitter:card': 'summary_large_image',
    'twitter:title': p.titulo,
    'twitter:description': p.descricao,
    'twitter:image': imagem,
  };

  for (const [nome, conteudo] of Object.entries(tags)) {
    const seletor = nome.startsWith('og:') ? `property="${nome}"` : `name="${nome}"`;
    meta.updateTag({ [nome.startsWith('og:') ? 'property' : 'name']: nome, content: conteudo }, seletor);
  }
}
