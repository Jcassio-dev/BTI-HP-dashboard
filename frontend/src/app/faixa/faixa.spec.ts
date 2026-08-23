import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Faixa } from './faixa';

describe('Faixa', () => {
  let fixture: ComponentFixture<Faixa>;
  let comp: Faixa;

  function montar(a: number, n: number, f: number, t: number, max: number) {
    fixture.componentRef.setInput('aprovados', a);
    fixture.componentRef.setInput('reprovadosNota', n);
    fixture.componentRef.setInput('reprovadosFalta', f);
    fixture.componentRef.setInput('trancados', t);
    fixture.componentRef.setInput('maxTotal', max);
    fixture.detectChanges();
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [Faixa] }).compileComponents();
    fixture = TestBed.createComponent(Faixa);
    comp = fixture.componentInstance;
  });

  it('divide os segmentos sobre o total de matriculados', () => {
    montar(165, 7, 2, 22, 196);

    expect(comp.total()).toBe(196);
    const pcts = comp.segmentos().map((s) => s.pct);
    expect(pcts[0]).toBeCloseTo((165 / 196) * 100, 6);
    expect(pcts[3]).toBeCloseTo((22 / 196) * 100, 6);
    expect(pcts.reduce((x, y) => x + y, 0)).toBeCloseTo(100, 6);
  });

  it('mantem a ordem aprovado, nota, falta, trancado', () => {
    montar(1, 2, 3, 4, 10);
    expect(comp.segmentos().map((s) => s.cor)).toEqual([
      'var(--aprovado)', 'var(--reprovado)', 'var(--falta)', 'var(--trancado)',
    ]);
  });

  it('a largura sai da raiz da razao contra o maior total visivel', () => {
    montar(50, 0, 0, 0, 200);
    expect(comp.fracao()).toBeCloseTo(Math.sqrt(50 / 200), 6);

    montar(200, 0, 0, 0, 200);
    expect(comp.fracao()).toBeCloseTo(1, 6);
  });

  it('descreve a faixa para leitor de tela', () => {
    montar(165, 7, 2, 22, 196);
    expect(comp.descricao()).toBe(
      '196 matriculados: 165 aprovados, 7 reprovados por nota, 2 por falta, 22 trancaram'
    );
  });

  it('usa singular e omite desfecho zerado', () => {
    montar(1, 0, 1, 0, 2);
    expect(comp.descricao()).toBe('2 matriculados: 1 aprovado, 1 por falta');
  });

  it('nao quebra com turma vazia', () => {
    montar(0, 0, 0, 0, 0);
    expect(comp.total()).toBe(0);
    expect(comp.segmentos()).toEqual([]);
    expect(comp.fracao()).toBe(0);
    expect(comp.descricao()).toBe('sem matriculados');
  });
});
