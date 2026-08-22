export function token(nome: string): string {
  if (typeof document === 'undefined') return '';
  return getComputedStyle(document.documentElement).getPropertyValue(nome).trim();
}

export interface Desfecho {
  aprovado: string;
  reprovado: string;
  falta: string;
  trancado: string;
}

export function coresDesfecho(): Desfecho {
  return {
    aprovado: token('--aprovado'),
    reprovado: token('--reprovado'),
    falta: token('--falta'),
    trancado: token('--trancado'),
  };
}
