import { Injectable, PLATFORM_ID, effect, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

export type Tema = 'claro' | 'escuro';

const CHAVE = 'bti-tema';
const PADRAO: Tema = 'escuro';

@Injectable({ providedIn: 'root' })
export class TemaService {
  private readonly navegador = isPlatformBrowser(inject(PLATFORM_ID));

  readonly tema = signal<Tema>(this.ler());

  constructor() {
    if (!this.navegador) return;
    effect(() => {
      const t = this.tema();
      document.documentElement.dataset['tema'] = t;
      try {
        localStorage.setItem(CHAVE, t);
      } catch {}
    });
  }

  alternar(): void {
    this.tema.set(this.tema() === 'escuro' ? 'claro' : 'escuro');
  }

  private ler(): Tema {
    if (!this.navegador) return PADRAO;
    try {
      return localStorage.getItem(CHAVE) === 'claro' ? 'claro' : PADRAO;
    } catch {
      return PADRAO;
    }
  }
}
