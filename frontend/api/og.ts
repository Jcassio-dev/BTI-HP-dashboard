import { ImageResponse } from '@vercel/og';

export const config = { runtime: 'edge' };

const API = 'https://btihelpbot.duckdns.org';

const PAPEL = '#F2F0EA';
const CARTAO = '#FBFAF6';
const TINTA = '#1A1917';
const TINTA_2 = '#55514A';
const TINTA_3 = '#6F6A5F';
const REGUA = '#DFDACE';
const CORES = ['#2F6B4C', '#B04A2F', '#7A3B24', '#CFC7B5'];

interface Linha {
  componenteCodigo: string | null;
  componenteNome: string | null;
  docenteNome: string | null;
  aprovados: number;
  reprovadosNota: number;
  reprovadosFalta: number;
  trancados: number;
  totalMatriculados: number;
  taxaAprovacao: number;
}

let fontes: { name: string; data: ArrayBuffer; weight: 400 | 600; style: 'normal' }[] | null = null;

async function carregarFontes() {
  if (fontes) return fontes;
  const [sans400, sans600, mono400] = await Promise.all([
    fetch(new URL('./fonts/plex-sans-400.woff', import.meta.url)).then((r) => r.arrayBuffer()),
    fetch(new URL('./fonts/plex-sans-600.woff', import.meta.url)).then((r) => r.arrayBuffer()),
    fetch(new URL('./fonts/plex-mono-400.woff', import.meta.url)).then((r) => r.arrayBuffer()),
  ]);
  fontes = [
    { name: 'Plex', data: sans400, weight: 400, style: 'normal' },
    { name: 'Plex', data: sans600, weight: 600, style: 'normal' },
    { name: 'PlexMono', data: mono400, weight: 400, style: 'normal' },
  ];
  return fontes;
}

export function titulo(texto: string | null): string {
  if (!texto) return '';
  const bruto = texto.trim();
  if (bruto !== bruto.toLocaleUpperCase('pt-BR')) return bruto;
  const menores = new Set(['de', 'da', 'do', 'das', 'dos', 'e', 'em', 'a', 'o', 'as', 'os', 'para', 'com']);
  const romano = /^(?:i{1,3}|iv|vi{0,3}|ix|xi{0,3})$/;
  return bruto
    .split(/\s+/)
    .map((p, i) => {
      const b = p.toLocaleLowerCase('pt-BR');
      if (romano.test(b)) return b.toLocaleUpperCase('pt-BR');
      if (i > 0 && menores.has(b)) return b;
      return b.charAt(0).toLocaleUpperCase('pt-BR') + b.slice(1);
    })
    .join(' ');
}

function pct(n: number): string {
  return `${Math.round(n * 100)}%`;
}

function faixa(l: Linha, largura: number) {
  const total = l.totalMatriculados || 1;
  const partes = [l.aprovados, l.reprovadosNota, l.reprovadosFalta, l.trancados];
  return {
    type: 'div',
    props: {
      style: { display: 'flex', width: `${largura}px`, height: '14px', borderRadius: '2px', overflow: 'hidden', background: REGUA },
      children: partes.map((n, i) => ({
        type: 'div',
        props: { style: { width: `${(n / total) * 100}%`, background: CORES[i] } },
      })),
    },
  };
}

function linhaTabela(l: Linha, rotulo: string, max: number) {
  const largura = Math.max(24, Math.sqrt((l.totalMatriculados || 0) / (max || 1)) * 190);
  return {
    type: 'div',
    props: {
      style: {
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        gap: '20px', padding: '12px 0', borderTop: `1px solid ${REGUA}`,
      },
      children: [
        {
          type: 'div',
          props: {
            style: { display: 'flex', fontSize: '26px', color: TINTA, flex: '1', overflow: 'hidden' },
            children: rotulo,
          },
        },
        { type: 'div', props: { style: { display: 'flex', width: '200px', justifyContent: 'flex-end' }, children: [faixa(l, largura)] } },
        {
          type: 'div',
          props: {
            style: { display: 'flex', fontSize: '28px', fontWeight: 600, color: TINTA, width: '90px', justifyContent: 'flex-end' },
            children: pct(l.taxaAprovacao),
          },
        },
        {
          type: 'div',
          props: {
            style: { display: 'flex', fontFamily: 'PlexMono', fontSize: '22px', color: TINTA_3, width: '80px', justifyContent: 'flex-end' },
            children: String(l.totalMatriculados),
          },
        },
      ],
    },
  };
}

export function pagina(eyebrow: string, nome: string, linhas: Linha[], rotulo: (l: Linha) => string, alunos: number) {
  const max = linhas.reduce((m, l) => Math.max(m, l.totalMatriculados), 0);
  return {
    type: 'div',
    props: {
      style: {
        display: 'flex', flexDirection: 'column', width: '1200px', height: '630px',
        background: PAPEL, padding: '56px 64px', fontFamily: 'Plex',
      },
      children: [
        {
          type: 'div',
          props: {
            style: { display: 'flex', fontFamily: 'PlexMono', fontSize: '22px', color: TINTA_3, letterSpacing: '0.06em' },
            children: eyebrow,
          },
        },
        {
          type: 'div',
          props: {
            style: { display: 'flex', fontSize: '64px', fontWeight: 600, color: TINTA, marginTop: '8px', lineHeight: 1.1 },
            children: nome,
          },
        },
        {
          type: 'div',
          props: {
            style: { display: 'flex', flexDirection: 'column', marginTop: '28px', background: CARTAO, padding: '4px 24px', borderRadius: '4px' },
            children: linhas.slice(0, 5).map((l) => linhaTabela(l, rotulo(l), max)),
          },
        },
        {
          type: 'div',
          props: {
            style: { display: 'flex', marginTop: 'auto', justifyContent: 'space-between', fontSize: '22px', color: TINTA_2 },
            children: [
              { type: 'div', props: { style: { display: 'flex' }, children: `${alunos} alunos nos últimos 10 semestres` } },
              { type: 'div', props: { style: { display: 'flex' }, children: 'dados.ufrn.br' } },
            ],
          },
        },
      ],
    },
  };
}

export default async function handler(req: Request) {
  const { searchParams } = new URL(req.url);
  const tipo = searchParams.get('tipo');
  const fonts = await carregarFontes();
  const opcoes = { width: 1200, height: 630, fonts };

  try {
    if (tipo === 'professor') {
      const slug = searchParams.get('slug') ?? '';
      const r = await fetch(`${API}/api/professor/${encodeURIComponent(slug)}`);
      if (!r.ok) throw new Error('professor');
      const p = await r.json();
      return new ImageResponse(
        pagina('Professor', titulo(p.nome), p.turmas, (l: Linha) => titulo(l.componenteNome), p.totalMatriculados),
        opcoes
      );
    }

    const codigo = searchParams.get('codigo') ?? '';
    const r = await fetch(`${API}/api/turma/${encodeURIComponent(codigo)}`);
    if (!r.ok) throw new Error('turma');
    const t = await r.json();
    return new ImageResponse(
      pagina(t.codigo ?? 'Disciplina', titulo(t.nome), t.professores, (l: Linha) => titulo(l.docenteNome), t.totalMatriculados),
      opcoes
    );
  } catch {
    return new ImageResponse(
      {
        type: 'div',
        props: {
          style: {
            display: 'flex', flexDirection: 'column', justifyContent: 'center',
            width: '1200px', height: '630px', background: PAPEL, padding: '64px', fontFamily: 'Plex',
          },
          children: [
            { type: 'div', props: { style: { display: 'flex', fontSize: '64px', fontWeight: 600, color: TINTA }, children: 'Taxa de aprovação' } },
            { type: 'div', props: { style: { display: 'flex', fontSize: '28px', color: TINTA_2, marginTop: '12px' }, children: 'Professores e disciplinas dos cursos de computação da UFRN' } },
            { type: 'div', props: { style: { display: 'flex', fontSize: '22px', color: TINTA_3, marginTop: 'auto' }, children: 'dados.ufrn.br' } },
          ],
        },
      },
      opcoes
    );
  }
}
