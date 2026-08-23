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

  it('comeca no escuro e o alternador oferece o claro', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const botao = (fixture.nativeElement as HTMLElement).querySelector('bti-tema-toggle button');
    expect(document.documentElement.dataset['tema']).toBe('escuro');
    expect(botao?.getAttribute('aria-label')).toBe('Mudar para o tema claro');
  });
});
