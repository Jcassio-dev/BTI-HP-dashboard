import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { App } from './app';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([])],
    }).compileComponents();
  });

  it('monta a casca da aplicacao', () => {
    const fixture = TestBed.createComponent(App);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('marca a aba ativa conforme a rota', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const abas = (fixture.nativeElement as HTMLElement).querySelectorAll('nav a');
    expect(abas.length).toBe(2);
    expect(abas[0].textContent?.trim()).toBe('Disciplinas');
    expect(abas[0].getAttribute('aria-current')).toBe('page');
    expect(abas[1].getAttribute('aria-current')).toBeNull();
  });

  it('comeca no escuro e o alternador oferece o claro', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const botao = (fixture.nativeElement as HTMLElement).querySelector('bti-tema-toggle button');
    expect(document.documentElement.dataset['tema']).toBe('escuro');
    expect(botao?.getAttribute('aria-label')).toBe('Mudar para o tema claro');
  });
});
