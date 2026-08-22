import { Injectable, PLATFORM_ID, computed, effect, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

export type Tema = 'sistema' | 'claro' | 'escuro';
export type TemaAplicado = 'claro' | 'escuro';

const CHAVE = 'bti-tema';
const ORDEM: Tema[] = ['sistema', 'claro', 'escuro'];

@Injectable({ providedIn: 'root' })
export class TemaService {
  private readonly navegador = isPlatformBrowser(inject(PLATFORM_ID));

  readonly preferencia = signal<Tema>(this.lerPreferencia());
  private readonly sistemaEscuro = signal(this.lerSistema());

  readonly aplicado = computed<TemaAplicado>(() => {
    const p = this.preferencia();
    return p === 'sistema' ? (this.sistemaEscuro() ? 'escuro' : 'claro') : p;
  });

  readonly proximo = computed<Tema>(
    () => ORDEM[(ORDEM.indexOf(this.preferencia()) + 1) % ORDEM.length]
  );

  constructor() {
    if (!this.navegador) return;

    matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) =>
      this.sistemaEscuro.set(e.matches)
    );

    effect(() => {
      document.documentElement.dataset['tema'] = this.aplicado();
      const p = this.preferencia();
      try {
        if (p === 'sistema') localStorage.removeItem(CHAVE);
        else localStorage.setItem(CHAVE, p);
      } catch {}
    });
  }

  alternar(): void {
    this.preferencia.set(this.proximo());
  }

  private lerPreferencia(): Tema {
    if (!this.navegador) return 'sistema';
    try {
      const v = localStorage.getItem(CHAVE);
      return v === 'claro' || v === 'escuro' ? v : 'sistema';
    } catch {
      return 'sistema';
    }
  }

  private lerSistema(): boolean {
    if (!this.navegador) return false;
    try {
      return matchMedia('(prefers-color-scheme: dark)').matches;
    } catch {
      return false;
    }
  }
}
