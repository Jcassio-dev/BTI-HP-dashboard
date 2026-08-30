import { Component, computed, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { TemaToggle } from './tema/tema-toggle';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, TemaToggle],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  private readonly router = inject(Router);

  private readonly url = signal(this.router.url);
  readonly aba = computed<'disciplinas' | 'bot' | 'nenhuma'>(() => {
    const u = this.url();
    if (u.startsWith('/bot')) return 'bot';
    if (u.startsWith('/conectar') || u.startsWith('/termos')) return 'nenhuma';
    return 'disciplinas';
  });

  constructor() {
    this.router.events
      .pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd))
      .subscribe((e) => this.url.set(e.urlAfterRedirects));
  }
}
